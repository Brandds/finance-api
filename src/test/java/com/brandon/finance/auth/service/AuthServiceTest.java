package com.brandon.finance.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.brandon.finance.auth.dto.ChangePasswordRequest;
import com.brandon.finance.auth.dto.LoginRequest;
import com.brandon.finance.auth.helper.AuthHelper;
import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.mapper.UserMapper;
import com.brandon.finance.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @Mock UserMapper userMapper;
    @Mock AuthHelper authHelper;
    @Mock AuthenticatedUserService authenticatedUserService;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AuthService service;

    private User user;
    private final ChangePasswordRequest request = new ChangePasswordRequest("current", "new-password", "new-password");

    @BeforeEach
    void setUp() {
        user = new User(42L);
        user.setPassword("stored-hash");
    }

    @Test
    void changesOnlyAuthenticatedUsersPasswordAndStoresHash() {
        when(authenticatedUserService.getUser()).thenReturn(user);
        when(passwordEncoder.matches("current", "stored-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        service.changePassword(request);

        assertThat(user.getPassword()).isEqualTo("new-hash");
        assertThat(user.getId()).isEqualTo(42L);
        verify(userRepository).save(user);
    }

    @Test
    void rejectsWrongCurrentPasswordWithoutSaving() {
        when(authenticatedUserService.getUser()).thenReturn(user);

        assertThatThrownBy(() -> service.changePassword(request))
            .isInstanceOf(BusinessException.class).hasMessage("Senha atual incorreta");

        assertThat(user.getPassword()).isEqualTo("stored-hash");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rejectsDifferentConfirmation() {
        assertThatThrownBy(() -> service.changePassword(new ChangePasswordRequest("current", "new-password", "other")))
            .isInstanceOfSatisfying(BusinessException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsPasswordReuse() {
        when(authenticatedUserService.getUser()).thenReturn(user);
        when(passwordEncoder.matches("current", "stored-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "stored-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(request))
            .isInstanceOf(BusinessException.class).hasMessage("A nova senha deve ser diferente da atual");
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsOAuthAccountWithoutLocalPassword() {
        user.setPassword(null);
        when(authenticatedUserService.getUser()).thenReturn(user);

        assertThatThrownBy(() -> service.changePassword(request))
            .isInstanceOf(BusinessException.class).hasMessage("Usuário não possui senha local");
        verify(userRepository, never()).save(any());
    }

    @Test
    void propagatesMissingAuthenticatedUser() {
        when(authenticatedUserService.getUser()).thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

        assertThatThrownBy(() -> service.changePassword(request)).isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void storesBCryptHashThatAcceptsOnlyNewPassword() {
        var encoder = new BCryptPasswordEncoder(4);
        user.setPassword(encoder.encode("current"));
        when(authenticatedUserService.getUser()).thenReturn(user);
        var realEncoderService = new AuthService(userRepository, jwtService, userMapper, authHelper,
            authenticatedUserService, encoder);

        realEncoderService.changePassword(request);

        assertThat(encoder.matches("new-password", user.getPassword())).isTrue();
        assertThat(encoder.matches("current", user.getPassword())).isFalse();
        assertThat(user.getPassword()).isNotEqualTo("new-password");
        verify(userRepository).save(user);
    }

    @Test
    void rejectsPasswordExceedingBCryptByteLimit() {
        String longUnicodePassword = "á".repeat(37);
        assertThatThrownBy(() -> service.changePassword(
                new ChangePasswordRequest("current", longUnicodePassword, longUnicodePassword)))
            .isInstanceOfSatisfying(BusinessException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsLoginBeforeEmailConfirmationWithoutIssuingJwt() {
        user.setEnabled(false);
        when(userRepository.findByEmail("owner@example.test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "stored-hash")).thenReturn(true);

        assertThatThrownBy(() -> loginService().login(new LoginRequest("owner@example.test", "password")))
            .isInstanceOfSatisfying(BusinessException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void issuesJwtAfterEmailConfirmation() {
        user.setEnabled(true);
        when(userRepository.findByEmail("owner@example.test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "stored-hash")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt");

        assertThat(loginService().login(new LoginRequest("owner@example.test", "password")).token())
            .isEqualTo("jwt");
    }

    private AuthService loginService() {
        return new AuthService(userRepository, jwtService, new UserMapper(), new AuthHelper(passwordEncoder),
            authenticatedUserService, passwordEncoder);
    }
}
