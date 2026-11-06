package com.brandon.finance.email.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.brandon.finance.email.entity.EmailVerificationToken;
import com.brandon.finance.email.mapper.EmailVerificationTokenMapper;
import com.brandon.finance.email.service.impl.EmailVerificationTokenServiceImpl;
import com.brandon.finance.shared.base.excepetion.BusinessException;
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
@ContextConfiguration(classes = EmailVerificationTransactionTest.JpaConfig.class)
@Import({EmailVerificationTokenServiceImpl.class, EmailVerificationTokenMapper.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EmailVerificationTransactionTest {
    @Configuration
    @EntityScan("com.brandon.finance")
    @EnableJpaRepositories(basePackageClasses = {EmailVerificationTokenRepository.class, UserRepository.class})
    static class JpaConfig {}

    @Autowired EmailVerificationTokenServiceImpl service;
    @Autowired UserRepository userRepository;
    @MockitoSpyBean EmailVerificationTokenRepository tokenRepository;
    @Autowired PlatformTransactionManager transactionManager;
    private TransactionTemplate transaction;

    @BeforeEach
    void createPendingUserAndToken() {
        transaction = new TransactionTemplate(transactionManager);
        transaction.executeWithoutResult(status -> {
            tokenRepository.deleteAll();
            userRepository.deleteAll();
            var user = userRepository.save(new User("Owner", "owner@example.test", "hash", Role.USER, "52998224725"));
            var token = new EmailVerificationToken();
            token.setToken("verification-token");
            token.setUser(user);
            token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            tokenRepository.save(token);
        });
    }

    @Test
    void concurrentConfirmationsConsumeTokenOnlyOnce() throws Exception {
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> verifyAfter(start));
            var second = executor.submit(() -> verifyAfter(start));
            start.countDown();

            assertThat(List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                .containsExactlyInAnyOrder("CONFIRMED", "INVALID_TOKEN");
        }

        transaction.executeWithoutResult(status -> {
            var token = tokenRepository.findByToken("verification-token").orElseThrow();
            assertThat(token.isUsed()).isTrue();
            assertThat(token.getUser().isEnabled()).isTrue();
        });
    }

    @Test
    void tokenSaveFailureRollsBackUserActivation() {
        doThrow(new DataAccessResourceFailureException("Test write failure"))
            .when(tokenRepository).save(any(EmailVerificationToken.class));

        assertThatThrownBy(() -> service.verifyEmail("verification-token"))
            .isInstanceOf(DataAccessResourceFailureException.class);

        transaction.executeWithoutResult(status -> {
            var token = tokenRepository.findByToken("verification-token").orElseThrow();
            assertThat(token.isUsed()).isFalse();
            assertThat(token.getUser().isEnabled()).isFalse();
        });
    }

    private String verifyAfter(CountDownLatch start) throws InterruptedException {
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Confirmation start timed out");
        }
        try {
            service.verifyEmail("verification-token");
            return "CONFIRMED";
        } catch (BusinessException error) {
            return error.getErrorCode().name();
        }
    }
}
