package com.brandon.finance.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.brandon.finance.auth.dto.ResetPasswordRequest;
import com.brandon.finance.auth.entity.PasswordResetToken;
import com.brandon.finance.auth.repository.PasswordResetTokenRepository;
import com.brandon.finance.email.service.EmailService;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {
    @Mock UserRepository users;
    @Mock PasswordResetTokenRepository tokens;
    @Mock PasswordEncoder encoder;
    @Mock EmailService email;
    @InjectMocks PasswordResetService service;
    User user;
    PasswordResetToken token;
    ResetPasswordRequest request = new ResetPasswordRequest("raw-token", "new-password", "new-password");

    @BeforeEach
    void setUp() {
        user = new User(42L);
        user.setEmail("owner@example.test");
        user.setEnabled(true);
        user.setPassword("old-hash");
        token = new PasswordResetToken();
        token.setUser(user);
        token.setPasswordHash("old-hash");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
    }

    @Test
    void sendsRandomTokenButPersistsOnlyItsHashWithExpiration() {
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        service.requestReset(user.getEmail());
        var raw = ArgumentCaptor.forClass(String.class);
        var saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(email).sendPasswordResetEmail(eq(user), raw.capture());
        verify(tokens).save(saved.capture());
        assertThat(raw.getValue()).hasSize(43);
        assertThat(saved.getValue().getTokenHash()).isEqualTo(PasswordResetService.hash(raw.getValue()))
            .isNotEqualTo(raw.getValue());
        assertThat(saved.getValue().getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(14))
            .isBefore(LocalDateTime.now().plusMinutes(16));
    }

    @Test
    void doesNotRevealUnknownDisabledOrOAuthAccounts() {
        when(users.findByEmail("unknown@example.test")).thenReturn(Optional.empty());
        service.requestReset("unknown@example.test");
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        user.setEnabled(false);
        service.requestReset(user.getEmail());
        user.setEnabled(true);
        user.setPassword(null);
        service.requestReset(user.getEmail());
        verifyNoInteractions(tokens, email);
    }

    @Test
    void storesBCryptPasswordAndConsumesTokenOnlyOnce() {
        var bcrypt = new BCryptPasswordEncoder(4);
        user.setPassword(bcrypt.encode("old-password"));
        token.setPasswordHash(user.getPassword());
        validToken();
        var realService = new PasswordResetService(users, tokens, bcrypt, email);
        realService.resetPassword(request);
        assertThat(bcrypt.matches(request.newPassword(), user.getPassword())).isTrue();
        assertThat(bcrypt.matches("old-password", user.getPassword())).isFalse();
        assertThat(token.isUsed()).isTrue();
        assertError(() -> realService.resetPassword(request), ErrorCode.INVALID_TOKEN);
        verify(users, times(1)).save(user);
    }

    @Test
    void rejectsUnknownToken() {
        assertError(() -> service.resetPassword(request), ErrorCode.INVALID_TOKEN);
        verify(users, never()).save(any());
    }

    @Test
    void rejectsExpiredToken() {
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        token.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        assertError(() -> service.resetPassword(request), ErrorCode.TOKEN_EXPIRED);
        verify(users, never()).save(any());
    }

    @Test
    void passwordChangeInvalidatesAllPreviouslyIssuedTokens() {
        validToken();
        user.setPassword("changed-hash");
        assertError(() -> service.resetPassword(request), ErrorCode.INVALID_TOKEN);
        verify(users, never()).save(any());
    }

    @Test
    void rejectsDisabledAccountAndPasswordReuse() {
        validToken();
        user.setEnabled(false);
        assertError(() -> service.resetPassword(request), ErrorCode.INVALID_TOKEN);
        user.setEnabled(true);
        when(encoder.matches(request.newPassword(), user.getPassword())).thenReturn(true);
        assertError(() -> service.resetPassword(request), ErrorCode.BUSINESS_ERROR);
        verify(users, never()).save(any());
    }

    @Test
    void rejectsMismatchedConfirmationAndMultibytePasswordAboveBCryptLimit() {
        assertError(() -> service.resetPassword(new ResetPasswordRequest("token", "new-password", "other")),
            ErrorCode.VALIDATION_ERROR);
        String tooLong = "á".repeat(37);
        assertError(() -> service.resetPassword(new ResetPasswordRequest("token", tooLong, tooLong)),
            ErrorCode.VALIDATION_ERROR);
        verifyNoInteractions(tokens, users, encoder);
    }

    private void validToken() {
        when(tokens.findByTokenHash(PasswordResetService.hash(request.token()))).thenReturn(Optional.of(token));
        when(users.findByIdForPasswordReset(42L)).thenReturn(Optional.of(user));
    }

    private void assertError(Runnable action, ErrorCode code) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(BusinessException.class,
            error -> assertThat(error.getErrorCode()).isEqualTo(code));
    }
}
