package com.brandon.finance.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.auth.dto.ResetPasswordRequest;
import com.brandon.finance.auth.entity.PasswordResetToken;
import com.brandon.finance.auth.repository.PasswordResetTokenRepository;
import com.brandon.finance.email.service.EmailService;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;
import com.brandon.finance.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email)
            .filter(user -> user.isEnabled() && user.getPassword() != null && !user.getPassword().isBlank())
            .ifPresent(user -> {
                byte[] bytes = new byte[32];
                random.nextBytes(bytes);
                String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                PasswordResetToken token = new PasswordResetToken();
                token.setTokenHash(hash(rawToken));
                token.setUser(user);
                token.setPasswordHash(user.getPassword());
                token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
                tokenRepository.save(token);
                emailService.sendPasswordResetEmail(user, rawToken);
            });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "As senhas não coincidem");
        }
        if (request.newPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "A senha deve ter no máximo 72 bytes em UTF-8");
        }
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(request.token()))
            .orElseThrow(PasswordResetService::invalidToken);
        if (token.isUsed()) {
            throw invalidToken();
        }
        if (!token.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "O token de recuperação expirou");
        }
        // Serializa redefinições com tokens diferentes da mesma conta.
        var user = userRepository.findByIdForPasswordReset(token.getUser().getId())
            .orElseThrow(PasswordResetService::invalidToken);
        if (!user.isEnabled() || !token.getPasswordHash().equals(user.getPassword())) {
            throw invalidToken();
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "A nova senha deve ser diferente da atual");
        }
        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        token.setUsed(true);
        userRepository.save(user);
        tokenRepository.save(token);
    }

    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponível", ex);
        }
    }

    private static BusinessException invalidToken() {
        return new BusinessException(ErrorCode.INVALID_TOKEN, "Token de recuperação inválido");
    }
}
