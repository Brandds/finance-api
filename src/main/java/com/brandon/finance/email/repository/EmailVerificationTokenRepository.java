package com.brandon.finance.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brandon.finance.email.entity.EmailVerificationToken;

import java.util.Optional;


public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);
    
}
