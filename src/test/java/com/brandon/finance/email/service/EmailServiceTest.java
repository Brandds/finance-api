package com.brandon.finance.email.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.brandon.finance.email.service.impl.EmailServiceImpl;
import com.brandon.finance.email.service.impl.EmailTemplateServiceImpl;
import com.brandon.finance.user.entity.User;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock JavaMailSender mailSender;
    @Mock EmailTemplateServiceImpl templateService;
    @InjectMocks EmailServiceImpl service;

    private User user;
    private MimeMessage message;

    @BeforeEach
    void setUp() {
        user = new User(42L);
        user.setName("Owner");
        user.setEmail("owner@example.test");
        message = new MimeMessage((Session) null);
        ReflectionTestUtils.setField(service, "apiUrl", "https://example.test");
        when(templateService.processTemplate(eq("email/verification-email"), anyMap()))
            .thenReturn("<p>Confirme seu cadastro</p>");
        when(mailSender.createMimeMessage()).thenReturn(message);
    }

    @ParameterizedTest
    @MethodSource("deliveryFailures")
    void doesNotInterruptRegistrationWhenDeliveryFails(MailException failure) {
        doThrow(failure).when(mailSender).send(message);

        assertThatCode(() -> service.sendVerificationEmail(user, "verification-token"))
            .doesNotThrowAnyException();

        verify(mailSender).send(message);
    }

    static Stream<MailException> deliveryFailures() {
        return Stream.of(new MailAuthenticationException("Authentication failed"),
            new MailSendException("SMTP unavailable"));
    }

    @Test
    void sendsVerificationEmailWhenDeliverySucceeds() {
        service.sendVerificationEmail(user, "verification-token");

        verify(mailSender).send(message);
    }
}
