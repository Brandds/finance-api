package com.brandon.finance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.brandon.finance.auth.controller.AuthController;
import com.brandon.finance.auth.dto.ChangePasswordRequest;
import com.brandon.finance.auth.security.JwtFilter;
import com.brandon.finance.auth.service.AuthService;
import com.brandon.finance.auth.service.JwtService;
import com.brandon.finance.config.SecurityConfig;
import com.brandon.finance.email.service.impl.EmailVerificationTokenServiceImpl;
import com.brandon.finance.expense.controller.ExpenseController;
import com.brandon.finance.expense.dto.ExpenseDTO;
import com.brandon.finance.expense.dto.ExpenseFilterRequest;
import com.brandon.finance.expense.enums.PeriodType;
import com.brandon.finance.expense.service.ExpenseService;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.CustomAccessDeniedHandler;
import com.brandon.finance.shared.base.excepetion.CustomAuthenticationEntryPoint;
import com.brandon.finance.shared.base.excepetion.GlobalExceptionHandler;
import com.brandon.finance.shared.base.excepetion.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(properties = "app.cors.allowed-origins=http://localhost:8081,http://localhost:19006")
@ContextConfiguration(classes = {AuthController.class, ExpenseController.class, SecurityConfig.class,
    JwtFilter.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class, GlobalExceptionHandler.class})
class ContractsWebTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CustomAccessDeniedHandler deniedHandler;
    @MockitoBean AuthService authService;
    @MockitoBean ExpenseService expenseService;
    @MockitoBean JwtService jwtService;
    @MockitoBean EmailVerificationTokenServiceImpl emailVerificationTokenService;

    private static final String PASSWORD_BODY = """
        {"currentPassword":"current","newPassword":"new-password","confirmPassword":"new-password"}
        """;

    private void authenticate() {
        when(jwtService.extractUserId("valid-token")).thenReturn(42L);
        when(jwtService.extractEmail("valid-token")).thenReturn("user@example.test");
        when(jwtService.extractRole("valid-token")).thenReturn("USER");
    }

    @Test
    void newEndpointsRequireAuthentication() throws Exception {
        mvc.perform(post("/api/v1/auth/change-password").contentType(MediaType.APPLICATION_JSON).content(PASSWORD_BODY))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
        mvc.perform(get("/api/v1/expenses/filter"))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
        verify(authService, never()).changePassword(any());
        verify(expenseService, never()).filter(any(), any());
    }

    @Test
    void invalidTokenIsRejectedBeforeController() throws Exception {
        doThrow(new UnauthorizedException(ErrorCode.INVALID_TOKEN, "Token inválido"))
            .when(jwtService).validateToken("bad-token");
        mvc.perform(get("/api/v1/expenses/filter").header(HttpHeaders.AUTHORIZATION, "Bearer bad-token"))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"));
        verify(expenseService, never()).filter(any(), any());
    }

    @Test
    void changesPasswordWithExistingEnvelope() throws Exception {
        authenticate();
        mvc.perform(post("/api/v1/auth/change-password").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON).content(PASSWORD_BODY))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty())
            .andExpect(jsonPath("$.message").value("Senha alterada com sucesso"));
        verify(authService).changePassword(new ChangePasswordRequest("current", "new-password", "new-password"));
    }

    @Test
    void validatesPasswordPayload() throws Exception {
        authenticate();
        mvc.perform(post("/api/v1/auth/change-password").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"\",\"newPassword\":\"short\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        verify(authService, never()).changePassword(any());
    }

    @Test
    void bindsMobileFiltersAndPageableToDedicatedRoute() throws Exception {
        authenticate();
        when(expenseService.filter(any(), any())).thenReturn(Page.empty());
        mvc.perform(get("/api/v1/expenses/filter").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .param("categoryIds", "1,2").param("periodType", "CUSTOM")
                .param("startDate", "2026-01-01").param("endDate", "2026-01-31")
                .param("minAmount", "10.50").param("maxAmount", "50.00")
                .param("page", "2").param("size", "5").param("sort", "amount,desc"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.message").value("Despesas filtradas com sucesso"));
        var filter = ArgumentCaptor.forClass(ExpenseFilterRequest.class);
        var pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(expenseService).filter(filter.capture(), pageable.capture());
        assertThat(filter.getValue().getCategoryIds()).isEqualTo(List.of(1L, 2L));
        assertThat(filter.getValue().getPeriodType()).isEqualTo(PeriodType.CUSTOM);
        assertThat(filter.getValue().getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(filter.getValue().getEndDate()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(filter.getValue().getMinAmount()).isEqualByComparingTo(new BigDecimal("10.50"));
        assertThat(filter.getValue().getMaxAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageable.getValue().getSort().getOrderFor("amount").isDescending()).isTrue();
        verify(expenseService, never()).getById(any());
    }

    @Test
    void invalidQueryValuesReturnValidationError() throws Exception {
        authenticate();
        for (String[] invalid : List.of(new String[]{"periodType", "UNKNOWN"},
                new String[]{"startDate", "not-a-date"}, new String[]{"categoryIds", "abc"},
                new String[]{"categoryIds", "-1"}, new String[]{"minAmount", "-1"})) {
            mvc.perform(get("/api/v1/expenses/filter").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                    .param(invalid[0], invalid[1]))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }
        verify(expenseService, never()).filter(any(), any());
    }

    @Test
    void onlyNumericIdsMatchExpenseIdRoutes() throws Exception {
        authenticate();
        when(expenseService.getById(123L)).thenReturn(new ExpenseDTO());
        mvc.perform(get("/api/v1/expenses/123").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
            .andExpect(status().isOk());
        mvc.perform(get("/api/v1/expenses/not-an-id").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
            .andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/expenses/not-an-id").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/expenses/not-an-id").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
            .andExpect(status().isNotFound());
        verify(expenseService).getById(123L);
    }

    @Test
    void allowsConfiguredPreflightWithoutToken() throws Exception {
        mvc.perform(options("/api/v1/expenses/filter").header(HttpHeaders.ORIGIN, "http://localhost:8081")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8081"))
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    void rejectsUnlistedOrigins() throws Exception {
        mvc.perform(options("/api/v1/expenses/filter").header(HttpHeaders.ORIGIN, "http://unlisted.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
            .andExpect(status().isForbidden())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void allowedOriginStillNeedsAuthentication() throws Exception {
        mvc.perform(get("/api/v1/expenses/filter").header(HttpHeaders.ORIGIN, "http://localhost:8081"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8081"));
    }

    @Test
    void corsDefaultsToNoAllowedOriginsAndRejectsWildcards() {
        var request = new MockHttpServletRequest("GET", "/api/v1/expenses/filter");
        var config = new SecurityConfig(null, null, null);
        assertThat(config.corsConfigurationSource("").getCorsConfiguration(request).getAllowedOrigins()).isEmpty();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> config.corsConfigurationSource("*"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void forbiddenResponseUsesApiErrorAndEscapesPath() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/v1/\"forbidden");
        var response = new MockHttpServletResponse();
        deniedHandler.handle(request, response, new AccessDeniedException("denied"));

        var body = objectMapper.readTree(response.getContentAsByteArray());
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(body.get("errorCode").asText()).isEqualTo("FORBIDDEN");
        assertThat(body.get("timestamp")).isNotNull();
        assertThat(body.has("error")).isFalse();
        assertThat(body.get("path").asText()).isEqualTo(request.getRequestURI());
    }
}
