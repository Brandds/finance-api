package com.brandon.finance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.brandon.finance.auth.controller.AuthController;
import com.brandon.finance.auth.helper.AuthHelper;
import com.brandon.finance.auth.security.JwtFilter;
import com.brandon.finance.auth.service.AuthService;
import com.brandon.finance.auth.service.JwtService;
import com.brandon.finance.authentication.service.impl.AuthenticatedUserServiceImpl;
import com.brandon.finance.category.controller.CategoryController;
import com.brandon.finance.category.entity.Category;
import com.brandon.finance.category.enums.CategoryIconEnum;
import com.brandon.finance.category.mapper.CategoryMapper;
import com.brandon.finance.category.repository.CategoryRepository;
import com.brandon.finance.category.service.CategoryService;
import com.brandon.finance.config.SecurityConfig;
import com.brandon.finance.email.entity.EmailVerificationToken;
import com.brandon.finance.email.mapper.EmailVerificationTokenMapper;
import com.brandon.finance.email.repository.EmailVerificationTokenRepository;
import com.brandon.finance.email.service.impl.EmailServiceImpl;
import com.brandon.finance.email.service.impl.EmailVerificationTokenServiceImpl;
import com.brandon.finance.shared.base.excepetion.CustomAccessDeniedHandler;
import com.brandon.finance.shared.base.excepetion.CustomAuthenticationEntryPoint;
import com.brandon.finance.shared.base.excepetion.GlobalExceptionHandler;
import com.brandon.finance.user.controller.UserController;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.enums.Role;
import com.brandon.finance.user.mapper.UserMapper;
import com.brandon.finance.user.repository.UserRepository;
import com.brandon.finance.user.service.UserService;

@WebMvcTest
@ContextConfiguration(classes = {UserController.class, CategoryController.class, AuthController.class,
    UserService.class, UserMapper.class, CategoryService.class, CategoryMapper.class,
    AuthService.class, AuthHelper.class, AuthenticatedUserServiceImpl.class,
    EmailVerificationTokenServiceImpl.class, EmailVerificationTokenMapper.class,
    SecurityConfig.class, JwtFilter.class, GlobalExceptionHandler.class,
    CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
class Issue7WebTest {
    @Autowired MockMvc mvc;
    @MockitoBean UserRepository userRepository;
    @MockitoBean CategoryRepository categoryRepository;
    @MockitoBean EmailVerificationTokenRepository tokenRepository;
    @MockitoBean EmailServiceImpl emailService;
    @MockitoBean JwtService jwtService;
    @MockitoBean PasswordEncoder passwordEncoder;

    @Test
    void affectedPrivateRoutesRequireAuthentication() throws Exception {
        mvc.perform(get("/api/v1/users/all")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/categories/name").param("name", "Food"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotListOtherUsers() throws Exception {
        authenticate("USER");

        mvc.perform(get("/api/v1/users/all").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
        verify(userRepository, never()).findAll();
    }

    @Test
    void adminCanListUsersWithoutExposingPasswords() throws Exception {
        authenticate("ADMIN");
        when(userRepository.findAll()).thenReturn(List.of(owner()));

        mvc.perform(get("/api/v1/users/all").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").value(42))
            .andExpect(jsonPath("$.data[0].password").doesNotExist())
            .andExpect(jsonPath("$.message").value("Usuarios encontrados com sucesso"));
    }

    @Test
    void updatesOwnedUserByIdUsingJwtPrincipal() throws Exception {
        authenticate("USER");
        var owner = owner();
        when(userRepository.findById(42L)).thenReturn(Optional.of(owner));

        mvc.perform(put("/api/v1/users/42").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated\",\"email\":\"updated@example.test\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Updated"))
            .andExpect(jsonPath("$.data.email").value("updated@example.test"));
        assertThat(owner.getPassword()).isEqualTo("stored-hash");
        verify(userRepository).save(owner);
    }

    @Test
    void validatesUserUpdateBeforeSaving() throws Exception {
        authenticate("USER");

        mvc.perform(put("/api/v1/users/42").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"email\":\"invalid\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void forbidsUpdatingAnotherUsersId() throws Exception {
        authenticate("USER");

        mvc.perform(put("/api/v1/users/43").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Other\",\"email\":\"other@example.test\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
        verify(userRepository, never()).findById(43L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void currentUserUsesJwtPrincipalWithoutCastingItToEntity() throws Exception {
        authenticate("USER");
        when(userRepository.findById(42L)).thenReturn(Optional.of(owner()));

        mvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(42));
    }

    @Test
    void categoryNameSearchIgnoresArbitraryUserIdAndUsesAuthenticatedUser() throws Exception {
        authenticate("USER");
        var category = new Category(7L);
        category.setName("Food");
        category.setIcon(CategoryIconEnum.FOOD);
        when(categoryRepository.findByUserIdAndName(42L, "Food")).thenReturn(category);

        mvc.perform(get("/api/v1/categories/name").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .param("name", "Food").param("userId", "43"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(7));
        verify(categoryRepository, never()).findByUserIdAndName(43L, "Food");
    }

    @Test
    void missingCategoryUsesStandardNotFoundError() throws Exception {
        authenticate("USER");

        mvc.perform(get("/api/v1/categories/name").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .param("name", "Missing"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void missingNameOrVerificationTokenIsAValidationError() throws Exception {
        authenticate("USER");

        mvc.perform(get("/api/v1/categories/name").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        mvc.perform(get("/api/v1/auth/verify-email"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void localRegistrationRequiresSingleEmailConfirmationBeforeLogin() throws Exception {
        when(passwordEncoder.encode("password")).thenReturn("stored-hash");
        when(passwordEncoder.matches("password", "stored-hash")).thenReturn(true);
        when(userRepository.save(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 42L);
            return user;
        });

        mvc.perform(post("/api/v1/users/register").contentType(MediaType.APPLICATION_JSON).content("""
            {"name":"Owner","email":"owner@example.test","password":"password","cpf":"52998224725"}
            """))
            .andExpect(status().isCreated());

        var captured = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captured.capture());
        var verificationToken = captured.getValue();
        User user = verificationToken.getUser();
        assertThat(user.isEnabled()).isFalse();
        when(userRepository.findByEmail("owner@example.test")).thenReturn(Optional.of(user));

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
            {"email":"owner@example.test","password":"password"}
            """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("EMAIL_NOT_VERIFIED"));
        verify(jwtService, never()).generateToken(any());

        when(tokenRepository.findByToken(verificationToken.getToken())).thenReturn(Optional.of(verificationToken));
        mvc.perform(get("/api/v1/auth/verify-email").param("token", verificationToken.getToken()))
            .andExpect(status().isOk());
        assertThat(user.isEnabled()).isTrue();
        assertThat(verificationToken.isUsed()).isTrue();

        when(jwtService.generateToken(user)).thenReturn("jwt");
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
            {"email":"owner@example.test","password":"password"}
            """))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.token").value("jwt"));

        mvc.perform(get("/api/v1/auth/verify-email").param("token", verificationToken.getToken()))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"));
        verify(userRepository, times(2)).save(user);
    }

    private void authenticate(String role) {
        when(jwtService.extractUserId("valid-token")).thenReturn(42L);
        when(jwtService.extractEmail("valid-token")).thenReturn("owner@example.test");
        when(jwtService.extractRole("valid-token")).thenReturn(role);
    }

    private User owner() {
        var user = new User(42L);
        user.setName("Owner");
        user.setEmail("owner@example.test");
        user.setPassword("stored-hash");
        user.setRole(Role.USER);
        user.setEnabled(true);
        return user;
    }
}
