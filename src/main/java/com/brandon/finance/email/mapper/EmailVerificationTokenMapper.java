package com.brandon.finance.email.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.brandon.finance.email.entity.EmailVerificationToken;
import com.brandon.finance.user.entity.User;

@Component
public class EmailVerificationTokenMapper {
 
    public EmailVerificationToken toCreateEntity(String token, User user) {
        EmailVerificationToken emailVerificationToken = new EmailVerificationToken();
        
        emailVerificationToken.setToken(token);
        emailVerificationToken.setUser(user);
        emailVerificationToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        
        return emailVerificationToken;
    }
}
