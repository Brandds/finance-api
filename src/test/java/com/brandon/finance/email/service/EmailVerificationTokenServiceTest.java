package com.brandon.finance.email.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.brandon.finance.email.entity.EmailVerificationToken;
import com.brandon.finance.email.mapper.EmailVerificationTokenMapper;
import com.brandon.finance.email.repository.EmailVerificationTokenRepository;
import com.brandon.finance.email.service.impl.EmailVerificationTokenServiceImpl;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmailVerificationTokenServiceTest {
    @Mock EmailVerificationTokenRepository tokenRepository;
    @Mock EmailVerificationTokenMapper mapper;
    @Mock UserRepository userRepository;
    @InjectMocks EmailVerificationTokenServiceImpl service;
    private User user;
    private EmailVerificationToken token;

    @BeforeEach
    void setUp() {
        user = new User(42L);
        user.setEnabled(false);
        token = new EmailVerificationToken();
        token.setToken("verification-token");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
    }

    @Test
    void enablesUserAndConsumesTokenOnlyOnce() {
        when(tokenRepository.findByToken("verification-token")).thenReturn(Optional.of(token));

        service.verifyEmail("verification-token");

        assertThat(user.isEnabled()).isTrue();
        assertThat(token.isUsed()).isTrue();
        assertThatThrownBy(() -> service.verifyEmail("verification-token"))
            .isInstanceOfSatisfying(BusinessException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN));
        verify(userRepository, times(1)).save(user);
        verify(tokenRepository, times(1)).save(token);
    }

    @Test
    void rejectsExpiredTokenWithoutEnablingUserOrConsumingToken() {
        token.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(tokenRepository.findByToken("verification-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verifyEmail("verification-token"))
            .isInstanceOfSatisfying(BusinessException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.TOKEN_EXPIRED));
        assertUnchanged();
    }

    @Test
    void rejectsUnknownTokenWithBusinessError() {
        assertThatThrownBy(() -> service.verifyEmail("unknown-token"))
            .isInstanceOfSatisfying(BusinessException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN));
        assertUnchanged();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void rejectsMissingOrBlankToken(String value) {
        assertThatThrownBy(() -> service.verifyEmail(value))
            .isInstanceOfSatisfying(BusinessException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN));
        verify(tokenRepository, never()).findByToken(any());
        assertUnchanged();
    }

    private void assertUnchanged() {
        assertThat(user.isEnabled()).isFalse();
        assertThat(token.isUsed()).isFalse();
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }
}
