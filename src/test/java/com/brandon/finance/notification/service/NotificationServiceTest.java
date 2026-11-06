package com.brandon.finance.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.notification.entity.Notification;
import com.brandon.finance.notification.enums.NotificationType;
import com.brandon.finance.notification.event.NotificationRequestedEvent;
import com.brandon.finance.notification.mapper.NotificationMapper;
import com.brandon.finance.notification.repository.NotificationRepository;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock NotificationRepository repository;
    @Mock AuthenticatedUserService authenticatedUserService;
    @Mock UserRepository userRepository;
    @Spy NotificationMapper mapper = new NotificationMapper();
    @InjectMocks NotificationService service;

    @Test
    void listsOnlyAuthenticatedUsersNotificationsWithoutLosingPagination() {
        var pageable = PageRequest.of(1, 2, Sort.by("createdAt").descending());
        var notification = notification();
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        when(repository.findByUserId(42L, pageable))
            .thenReturn(new PageImpl<>(List.of(notification), pageable, 3));

        var result = service.getByUser(pageable);

        assertThat(result.getContent()).containsExactly(mapper.toDTO(notification));
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getSort()).isEqualTo(pageable.getSort());
    }

    @Test
    void countsUnreadNotificationsOnlyForAuthenticatedUser() {
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        when(repository.countByUserIdAndReadFalse(42L)).thenReturn(3L);

        assertThat(service.countUnread()).isEqualTo(3);
    }

    @Test
    void marksOwnedNotificationReadAndPersistsTimestamp() {
        var notification = notification();
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        when(repository.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(notification));

        var response = service.markAsRead(7L);

        assertThat(response.read()).isTrue();
        assertThat(response.readAt()).isNotNull();
        assertThat(notification.isRead()).isTrue();
        verify(repository).save(notification);
    }

    @Test
    void repeatedReadPreservesOriginalReadTimestamp() {
        var notification = notification();
        var firstRead = LocalDateTime.of(2026, 1, 1, 12, 0);
        notification.markAsRead(firstRead);
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        when(repository.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(notification));

        assertThat(service.markAsRead(7L).readAt()).isEqualTo(firstRead);
        verify(repository, never()).save(any());
    }

    @Test
    void treatsAnotherUsersNotificationAsNotFoundWithoutSaving() {
        when(authenticatedUserService.getUserId()).thenReturn(42L);

        assertThatThrownBy(() -> service.markAsRead(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(repository).findByIdAndUserId(99L, 42L);
        verify(repository, never()).save(any());
    }

    @Test
    void marksAllReadOnlyForAuthenticatedUser() {
        when(authenticatedUserService.getUserId()).thenReturn(42L);

        service.markAllAsRead();

        verify(repository).markAllAsRead(eq(42L), any(LocalDateTime.class));
    }

    @ParameterizedTest
    @EnumSource(NotificationType.class)
    void createsUnreadNotificationFromBudgetOrSecurityEvent(NotificationType type) {
        var user = new User(42L);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        service.create(new NotificationRequestedEvent(42L, type, "Alerta", "Mensagem"));

        var saved = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getUser()).isSameAs(user);
        assertThat(saved.getValue().getType()).isEqualTo(type);
        assertThat(saved.getValue().getTitle()).isEqualTo("Alerta");
        assertThat(saved.getValue().getMessage()).isEqualTo("Mensagem");
        assertThat(saved.getValue().isRead()).isFalse();
        assertThat(saved.getValue().getReadAt()).isNull();
        verifyNoInteractions(authenticatedUserService);
    }

    @Test
    void rejectsInvalidEventBeforeAccessingDatabase() {
        assertThatThrownBy(() -> service.create(new NotificationRequestedEvent(
            42L, NotificationType.BUDGET_ALERT, "", "Mensagem")))
            .isInstanceOfSatisfying(BusinessException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
        verifyNoInteractions(repository, userRepository);
    }

    @Test
    void rejectsUnknownEventRecipientWithoutSaving() {
        assertThatThrownBy(() -> service.create(new NotificationRequestedEvent(
            99L, NotificationType.SECURITY_ALERT, "Alerta", "Mensagem")))
            .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    private Notification notification() {
        var notification = new Notification();
        notification.setUser(new User(42L));
        notification.setType(NotificationType.BUDGET_ALERT);
        notification.setTitle("Orçamento próximo do limite");
        notification.setMessage("Confira seu orçamento mensal.");
        return notification;
    }
}
