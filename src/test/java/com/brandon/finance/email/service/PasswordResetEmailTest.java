package com.brandon.finance.email.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import com.brandon.finance.email.service.impl.EmailServiceImpl;
import com.brandon.finance.email.service.impl.EmailTemplateServiceImpl;
import com.brandon.finance.user.entity.User;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class PasswordResetEmailTest {
    @Mock JavaMailSender sender;
    @Mock EmailTemplateServiceImpl templates;
    @InjectMocks EmailServiceImpl service;
    User user;
    MimeMessage message;

    @BeforeEach
    void setUp() {
        user = new User(42L);
        user.setName("Owner");
        user.setEmail("owner@example.test");
        message = new MimeMessage((Session) null);
        when(sender.createMimeMessage()).thenReturn(message);
        when(templates.processTemplate(eq("email/password-reset-email"), anyMap()))
            .thenReturn("<p>Token de recuperação</p>");
    }

    @Test
    void deliversRecoveryTokenToAccountEmail() throws Exception {
        service.sendPasswordResetEmail(user, "raw-token");
        verify(templates).processTemplate("email/password-reset-email", Map.of("name", "Owner", "token", "raw-token"));
        verify(sender).send(message);
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(user.getEmail());
        assertThat(message.getSubject()).isEqualTo("Recuperação de senha");
    }

    @Test
    void smtpFailurePreservesGenericRecoveryResponse() {
        doThrow(new MailSendException("SMTP unavailable")).when(sender).send(message);
        assertThatCode(() -> service.sendPasswordResetEmail(user, "raw-token")).doesNotThrowAnyException();
    }
}
