package com.brandon.finance;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.brandon.finance.auth.controller.AuthController;
import com.brandon.finance.auth.controller.PasswordResetController;
import com.brandon.finance.auth.entity.PasswordResetToken;
import com.brandon.finance.auth.helper.AuthHelper;
import com.brandon.finance.auth.repository.PasswordResetTokenRepository;
import com.brandon.finance.auth.security.JwtFilter;
import com.brandon.finance.auth.service.AuthService;
import com.brandon.finance.auth.service.JwtService;
import com.brandon.finance.auth.service.PasswordResetService;
import com.brandon.finance.authentication.service.impl.AuthenticatedUserServiceImpl;
import com.brandon.finance.config.SecurityConfig;
import com.brandon.finance.email.service.EmailService;
import com.brandon.finance.email.service.impl.EmailVerificationTokenServiceImpl;
import com.brandon.finance.shared.base.excepetion.*;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.mapper.UserMapper;
import com.brandon.finance.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@WebMvcTest
@ContextConfiguration(classes = {AuthController.class, PasswordResetController.class,
    AuthService.class, PasswordResetService.class, AuthHelper.class, UserMapper.class,
    AuthenticatedUserServiceImpl.class, SecurityConfig.class, JwtFilter.class,
    GlobalExceptionHandler.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
class PasswordWebTest {
    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @MockitoBean UserRepository users;
    @MockitoBean PasswordResetTokenRepository tokens;
    @MockitoBean EmailService email;
    @MockitoBean EmailVerificationTokenServiceImpl verification;
    @MockitoBean JwtService jwt;

    @Test
    void recoveryWorksWithoutSessionAndDoesNotExposeAccountExistence() throws Exception {
        var user = user();
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        var known = mvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"owner@example.test\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        mvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"unknown@example.test\"}"))
            .andExpect(status().isOk()).andExpect(content().json(known));
        verify(email).sendPasswordResetEmail(eq(user), anyString());
    }

    @Test
    void resetAcceptsValidTokenWithoutSessionAndRejectsReuse() throws Exception {
        var user = user();
        var token = new PasswordResetToken();
        token.setUser(user);
        token.setPasswordHash(user.getPassword());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(users.findByIdForPasswordReset(42L)).thenReturn(Optional.of(user));
        String body = "{\"token\":\"valid\",\"newPassword\":\"new-password\",\"confirmPassword\":\"new-password\"}";
        mvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());
        org.assertj.core.api.Assertions.assertThat(encoder.matches("new-password", user.getPassword())).isTrue();
        mvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"));
    }

    @Test
    void returnsStandardErrorsForInvalidAndExpiredTokens() throws Exception {
        String body = "{\"token\":\"invalid\",\"newPassword\":\"new-password\",\"confirmPassword\":\"new-password\"}";
        mvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"));
        var token = new PasswordResetToken();
        token.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        mvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("TOKEN_EXPIRED"));
    }

    @Test
    void validatesRecoveryInputs() throws Exception {
        mvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"invalid\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        mvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"\",\"newPassword\":\"short\",\"confirmPassword\":\"short\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        verifyNoInteractions(tokens);
    }

    @Test
    void changeRequiresSessionThenCurrentPasswordAndStoresHash() throws Exception {
        String body = "{\"currentPassword\":\"old-password\",\"newPassword\":\"new-password\",\"confirmPassword\":\"new-password\"}";
        mvc.perform(post("/api/v1/auth/change-password").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnauthorized());
        when(jwt.extractUserId("jwt")).thenReturn(42L);
        when(jwt.extractEmail("jwt")).thenReturn("owner@example.test");
        when(jwt.extractRole("jwt")).thenReturn("USER");
        var user = user();
        when(users.findById(42L)).thenReturn(Optional.of(user));
        mvc.perform(post("/api/v1/auth/change-password").header("Authorization", "Bearer jwt")
            .contentType(MediaType.APPLICATION_JSON).content(body.replace("old-password", "wrong-password")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("BUSINESS_ERROR"))
            .andExpect(jsonPath("$.message").value("Senha atual incorreta"));
        mvc.perform(post("/api/v1/auth/change-password").header("Authorization", "Bearer jwt")
            .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        org.assertj.core.api.Assertions.assertThat(encoder.matches("new-password", user.getPassword())).isTrue();
        verify(users).save(user);
    }

    private User user() {
        var user = new User(42L);
        user.setEmail("owner@example.test");
        user.setEnabled(true);
        user.setPassword(encoder.encode("old-password"));
        return user;
    }
}
