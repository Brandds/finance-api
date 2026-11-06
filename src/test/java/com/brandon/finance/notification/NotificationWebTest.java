package com.brandon.finance.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.brandon.finance.auth.security.JwtFilter;
import com.brandon.finance.auth.service.JwtService;
import com.brandon.finance.authentication.service.impl.AuthenticatedUserServiceImpl;
import com.brandon.finance.config.SecurityConfig;
import com.brandon.finance.notification.controller.NotificationController;
import com.brandon.finance.notification.entity.Notification;
import com.brandon.finance.notification.enums.NotificationType;
import com.brandon.finance.notification.mapper.NotificationMapper;
import com.brandon.finance.notification.repository.NotificationRepository;
import com.brandon.finance.notification.service.NotificationService;
import com.brandon.finance.shared.base.excepetion.CustomAccessDeniedHandler;
import com.brandon.finance.shared.base.excepetion.CustomAuthenticationEntryPoint;
import com.brandon.finance.shared.base.excepetion.GlobalExceptionHandler;
import com.brandon.finance.shared.base.response.PageSerializer;
import com.brandon.finance.user.repository.UserRepository;

@WebMvcTest
@ContextConfiguration(classes = {NotificationController.class, NotificationService.class, NotificationMapper.class,
    AuthenticatedUserServiceImpl.class, SecurityConfig.class, JwtFilter.class, PageSerializer.class,
    GlobalExceptionHandler.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
class NotificationWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean NotificationRepository repository;
    @MockitoBean UserRepository userRepository;
    @MockitoBean JwtService jwtService;

    @Test
    void allEndpointsRequireAuthentication() throws Exception {
        mvc.perform(get("/api/v1/notifications")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/notifications/unread-count")).andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/v1/notifications/7/read")).andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/v1/notifications/read-all")).andExpect(status().isUnauthorized());
        verifyNoInteractions(repository);
    }

    @Test
    void listUsesJwtUserAndPreservesPaginationEnvelope() throws Exception {
        authenticate();
        when(repository.findByUserId(eq(42L), any())).thenReturn(new PageImpl<>(List.of(notification()),
            PageRequest.of(1, 2, Sort.by("createdAt").descending()), 5));

        mvc.perform(get("/api/v1/notifications").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .param("page", "1").param("size", "2").param("sort", "createdAt,desc").param("userId", "43"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[0].id").value(7))
            .andExpect(jsonPath("$.data.content[0].type").value("BUDGET_ALERT"))
            .andExpect(jsonPath("$.data.content[0].read").value(false))
            .andExpect(jsonPath("$.data.content[0].user").doesNotExist())
            .andExpect(jsonPath("$.data.totalElements").value(5))
            .andExpect(jsonPath("$.data.number").value(1))
            .andExpect(jsonPath("$.data.size").value(2));
        var pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByUserId(eq(42L), pageable.capture());
        assertThat(pageable.getValue().getOffset()).isEqualTo(2);
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt").isDescending()).isTrue();
    }

    @Test
    void defaultListOrderIsNewestFirstWithIdTiebreaker() throws Exception {
        authenticate();
        when(repository.findByUserId(eq(42L), any())).thenReturn(org.springframework.data.domain.Page.empty());

        mvc.perform(get("/api/v1/notifications").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
            .andExpect(status().isOk());

        var pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByUserId(eq(42L), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20);
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    }

    @Test
    void countsOnlyAuthenticatedUsersUnreadNotifications() throws Exception {
        authenticate();
        when(repository.countByUserIdAndReadFalse(42L)).thenReturn(2L);

        mvc.perform(get("/api/v1/notifications/unread-count")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token").param("userId", "43"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(2));
        verify(repository).countByUserIdAndReadFalse(42L);
    }

    @Test
    void marksOwnedNotificationAsRead() throws Exception {
        authenticate();
        var notification = notification();
        when(repository.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(notification));

        mvc.perform(patch("/api/v1/notifications/7/read").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.read").value(true))
            .andExpect(jsonPath("$.data.readAt").exists());
        verify(repository).save(notification);
    }

    @Test
    void anotherUsersNotificationIsNotFound() throws Exception {
        authenticate();

        mvc.perform(patch("/api/v1/notifications/99/read").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        verify(repository).findByIdAndUserId(99L, 42L);
        verify(repository, never()).save(any());
    }

    @Test
    void invalidIdIsRejectedBeforeQueryingDatabase() throws Exception {
        authenticate();

        mvc.perform(patch("/api/v1/notifications/0/read").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        verifyNoInteractions(repository);
    }

    @Test
    void markAllReadUsesAuthenticatedUser() throws Exception {
        authenticate();

        mvc.perform(patch("/api/v1/notifications/read-all")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token").param("userId", "43"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
        verify(repository).markAllAsRead(eq(42L), any(LocalDateTime.class));
    }

    private void authenticate() {
        when(jwtService.extractUserId("valid-token")).thenReturn(42L);
        when(jwtService.extractEmail("valid-token")).thenReturn("owner@example.test");
        when(jwtService.extractRole("valid-token")).thenReturn("USER");
    }

    private Notification notification() {
        var notification = new Notification();
        ReflectionTestUtils.setField(notification, "id", 7L);
        ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.of(2026, 1, 1, 12, 0));
        notification.setType(NotificationType.BUDGET_ALERT);
        notification.setTitle("Orçamento próximo do limite");
        notification.setMessage("Confira seu orçamento mensal.");
        return notification;
    }
}
