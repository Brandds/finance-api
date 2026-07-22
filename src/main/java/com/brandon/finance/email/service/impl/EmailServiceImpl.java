package com.brandon.finance.email.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.brandon.finance.email.service.EmailService;
import com.brandon.finance.user.entity.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl  implements EmailService {

    @Value("${app.api-url}")
    private String apiUrl;
    private final String verificationEmail = "/auth/verify-email?token=";
    
    private final JavaMailSender mailSender;
    private final EmailTemplateServiceImpl templateService;

    @Override
    public void sendVerificationEmail(User user, String token) {

        String url = apiUrl + verificationEmail +   token;

        Map<String, Object> variables = Map.of(
                "name", user.getName(),
                "verificationUrl", url,
                "expiration", "15 minutos"
        );

        String html = templateService.processTemplate(
                "email/verification-email",
                variables
        );

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {

            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setTo(user.getEmail());
            helper.setSubject("Confirme seu cadastro");
            helper.setText(html, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {

            throw new RuntimeException("Erro ao enviar e-mail.", e);

        }
    }
}
