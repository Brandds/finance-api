package com.brandon.finance.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.notification.entity.Notification;
import com.brandon.finance.notification.enums.NotificationType;
import com.brandon.finance.notification.event.NotificationEventListener;
import com.brandon.finance.notification.event.NotificationRequestedEvent;
import com.brandon.finance.notification.mapper.NotificationMapper;
import com.brandon.finance.notification.service.NotificationService;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.enums.Role;
import com.brandon.finance.user.repository.UserRepository;

/** Executar somente em PostgreSQL descartável chamado finance_contracts_test. */
@EnabledIfEnvironmentVariable(named = "TEST_DATABASE_URL", matches = "jdbc:postgresql://.*/finance_contracts_test")
@DataJpaTest(showSql = false, properties = {
    "spring.datasource.url=${TEST_DATABASE_URL}",
    "spring.datasource.username=contracts_test",
    "spring.datasource.password=test-only-password",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true",
    "spring.liquibase.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = NotificationRepositoryTest.JpaConfig.class)
@Import({NotificationService.class, NotificationMapper.class, NotificationEventListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NotificationRepositoryTest {
    @Configuration
    @EntityScan("com.brandon.finance")
    @EnableJpaRepositories(basePackageClasses = {NotificationRepository.class, UserRepository.class})
    @EnableJpaAuditing
    static class JpaConfig {}

    @Autowired TestEntityManager entityManager;
    @Autowired NotificationService service;
    @Autowired ApplicationEventPublisher publisher;
    @MockitoBean AuthenticatedUserService authenticatedUserService;
    private User owner;
    private User other;

    @BeforeEach
    void setUp() {
        owner = entityManager.persist(new User("Owner", "owner@example.test", "hash", Role.USER, "11111111111"));
        other = entityManager.persist(new User("Other", "other@example.test", "hash", Role.USER, "22222222222"));
        when(authenticatedUserService.getUserId()).thenReturn(owner.getId());
    }

    @Test
    void readStatePersistsAndCounterDecreasesWithoutChangingAnotherUsersNotifications() {
        var first = notification(owner);
        notification(owner);
        var othersNotification = notification(other);
        entityManager.flush();
        entityManager.clear();

        assertThat(service.getByUser(PageRequest.of(0, 1)).getTotalElements()).isEqualTo(2);
        assertThat(service.countUnread()).isEqualTo(2);

        service.markAsRead(first.getId());
        entityManager.flush();
        entityManager.clear();
        var persisted = entityManager.find(Notification.class, first.getId());
        var firstReadAt = persisted.getReadAt();
        assertThat(persisted.isRead()).isTrue();
        assertThat(firstReadAt).isNotNull();
        assertThat(service.countUnread()).isEqualTo(1);

        service.markAllAsRead();
        entityManager.flush();
        entityManager.clear();
        assertThat(service.countUnread()).isZero();
        assertThat(entityManager.find(Notification.class, othersNotification.getId()).isRead()).isFalse();
        assertThat(entityManager.find(Notification.class, first.getId()).getReadAt()).isEqualTo(firstReadAt);

        service.markAllAsRead();
        assertThat(service.countUnread()).isZero();
    }

    @Test
    void cannotReadAnotherUsersNotificationById() {
        var othersNotification = notification(other);
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> service.markAsRead(othersNotification.getId()))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThat(entityManager.find(Notification.class, othersNotification.getId()).isRead()).isFalse();
    }

    @Test
    void publishedBudgetAndSecurityEventsArePersistedUnread() {
        for (var type : NotificationType.values()) {
            publisher.publishEvent(new NotificationRequestedEvent(owner.getId(), type, "Alerta", "Mensagem"));
        }
        entityManager.flush();
        entityManager.clear();

        var page = service.getByUser(PageRequest.of(0, 20));
        assertThat(page.getContent()).extracting(dto -> dto.type())
            .containsExactlyInAnyOrder(NotificationType.BUDGET_ALERT, NotificationType.SECURITY_ALERT);
        assertThat(service.countUnread()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(dto -> !dto.read() && dto.readAt() == null);
    }

    private Notification notification(User user) {
        var notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.BUDGET_ALERT);
        notification.setTitle("Alerta de orçamento");
        notification.setMessage("Confira seu orçamento mensal.");
        return entityManager.persist(notification);
    }
}
