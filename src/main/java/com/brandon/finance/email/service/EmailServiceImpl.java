package com.brandon.finance.email.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.brandon.finance.email.service.impl.EmailService;
import com.brandon.finance.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl  implements EmailService {

    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    private final JavaMailSender mailSender;


    @Override
    public void sendVerificationEmail(User user, String token) {
        String url = frontendUrl + "/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(user.getEmail());
        message.setSubject("Confirme seu cadastro");

        message.setText("""
                Olá!

                Clique no link abaixo para confirmar seu cadastro.

                %s

                O link expira em 15 minutos.
                """.formatted(url));

        mailSender.send(message);
    
    }
}
