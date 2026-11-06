package com.brandon.finance.email.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.email.entity.EmailVerificationToken;
import com.brandon.finance.email.mapper.EmailVerificationTokenMapper;
import com.brandon.finance.email.repository.EmailVerificationTokenRepository;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationTokenServiceImpl {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailVerificationTokenMapper emailVerificationTokenMapper;
    private final UserRepository userRepository;

    @Transactional
    public EmailVerificationToken create(String token, User user) {
        return save(emailVerificationTokenMapper.toCreateEntity(token, user));
    }
    

    public EmailVerificationToken save(EmailVerificationToken emailVerificationToken) {
        return emailVerificationTokenRepository.save(emailVerificationToken);
    }

    @Transactional
    public void verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Token inválido.");
        }

        EmailVerificationToken verificationToken =
                emailVerificationTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.INVALID_TOKEN,
                                        "Token inválido."
                                ));

        if (verificationToken.isUsed()) {
            throw new BusinessException(
                    ErrorCode.INVALID_TOKEN,
                    "Este link já foi utilizado."
            );
        }

        if (!verificationToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException(
                    ErrorCode.TOKEN_EXPIRED,
                    "O link expirou."
            );
        }

        User user = verificationToken.getUser();

        user.setEnabled(true);

        verificationToken.setUsed(true);

        userRepository.save(user);
        save(verificationToken);
    }
}
