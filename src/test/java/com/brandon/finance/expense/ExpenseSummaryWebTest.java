package com.brandon.finance.expense;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.brandon.finance.auth.security.JwtFilter;
import com.brandon.finance.auth.service.JwtService;
import com.brandon.finance.authentication.service.impl.AuthenticatedUserServiceImpl;
import com.brandon.finance.config.SecurityConfig;
import com.brandon.finance.expense.controller.ExpenseController;
import com.brandon.finance.expense.mapper.ExpenseMapper;
import com.brandon.finance.expense.repository.ExpenseRepository;
import com.brandon.finance.expense.repository.CategoryExpenseTotal;
import com.brandon.finance.account.repository.AccountRepository;
import com.brandon.finance.category.repository.CategoryRepository;
import com.brandon.finance.expense.service.ExpenseService;
import com.brandon.finance.shared.base.excepetion.CustomAccessDeniedHandler;
import com.brandon.finance.shared.base.excepetion.CustomAuthenticationEntryPoint;
import com.brandon.finance.shared.base.excepetion.GlobalExceptionHandler;
import com.brandon.finance.user.repository.UserRepository;

@WebMvcTest
@ContextConfiguration(classes = {ExpenseController.class, ExpenseService.class, AuthenticatedUserServiceImpl.class,
    SecurityConfig.class, JwtFilter.class, GlobalExceptionHandler.class,
    CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
class ExpenseSummaryWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean ExpenseRepository repository;
    @MockitoBean CategoryRepository categoryRepository;
    @MockitoBean AccountRepository accountRepository;
    @MockitoBean ExpenseMapper mapper;
    @MockitoBean UserRepository userRepository;
    @MockitoBean JwtService jwtService;

    @ParameterizedTest
    @ValueSource(strings = {"summary", "get-expense-analysis"})
    void requiresAuthentication(String route) throws Exception {
        mvc.perform(get("/api/v1/expenses/" + route)
                .param("startDate", "2026-03-01").param("endDate", "2026-03-31"))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"summary", "get-expense-analysis"})
    void returnsAggregateWithExistingEnvelopeAndUsesOnlyJwtUser(String route) throws Exception {
        authenticate();
        when(repository.summarizeByCategory(eq(42L), any(), any())).thenReturn(List.of());
        when(repository.sumByUserAndPeriod(eq(42L), any(), any())).thenReturn(new BigDecimal("20"));

        mvc.perform(get("/api/v1/expenses/" + route).header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .param("startDate", "2026-03-01").param("endDate", "2026-03-31").param("userId", "43"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalSpent").value(0))
            .andExpect(jsonPath("$.data.previousPeriodTotal").value(20))
            .andExpect(jsonPath("$.data.percentageChange").value(-100))
            .andExpect(jsonPath("$.data.startDate").value("2026-03-01"))
            .andExpect(jsonPath("$.data.endDate").value("2026-03-31"))
            .andExpect(jsonPath("$.data.previousStartDate").value("2026-01-29"))
            .andExpect(jsonPath("$.data.previousEndDate").value("2026-02-28"))
            .andExpect(jsonPath("$.data.categories").isEmpty())
            .andExpect(jsonPath("$.data.insight").isEmpty())
            .andExpect(jsonPath("$.message").value("Análise de despesas realizada com sucesso"));
        verify(repository).summarizeByCategory(42L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));
        verify(repository).sumByUserAndPeriod(42L, LocalDate.of(2026, 1, 29), LocalDate.of(2026, 2, 28));
    }

    @Test
    void serializesCategoryValuesPercentagesAndInsight() throws Exception {
        authenticate();
        var category = mock(CategoryExpenseTotal.class);
        when(category.getCategoryId()).thenReturn(7L);
        when(category.getCategoryName()).thenReturn("Alimentação");
        when(category.getAmount()).thenReturn(new BigDecimal("10.50"));
        when(repository.summarizeByCategory(eq(42L), any(), any())).thenReturn(List.of(category));
        when(repository.sumByUserAndPeriod(eq(42L), any(), any())).thenReturn(BigDecimal.ZERO);

        mvc.perform(get("/api/v1/expenses/summary").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .param("startDate", "2026-03-01").param("endDate", "2026-03-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalSpent").value(10.50))
            .andExpect(jsonPath("$.data.categories.length()").value(1))
            .andExpect(jsonPath("$.data.categories[0].categoryId").value(7))
            .andExpect(jsonPath("$.data.categories[0].categoryName").value("Alimentação"))
            .andExpect(jsonPath("$.data.categories[0].amount").value(10.50))
            .andExpect(jsonPath("$.data.categories[0].percentage").value(100))
            .andExpect(jsonPath("$.data.insight.type").value("LARGEST_CATEGORY"))
            .andExpect(jsonPath("$.data.insight.categoryId").value(7))
            .andExpect(jsonPath("$.data.insight.amount").value(10.50))
            .andExpect(jsonPath("$.data.insight.percentage").value(100));
    }

    @Test
    void rejectsMissingMalformedAndReversedDatesWithValidationError() throws Exception {
        authenticate();
        for (String[] dates : List.of(new String[]{"", "2026-03-31"}, new String[]{"2026-03-01", ""},
                new String[]{"not-a-date", "2026-03-31"}, new String[]{"2026-03-01", "2026-02-30"},
                new String[]{"2026-03-31", "2026-03-01"})) {
            mvc.perform(get("/api/v1/expenses/summary").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                    .param("startDate", dates[0]).param("endDate", dates[1]))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }
        mvc.perform(get("/api/v1/expenses/summary").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        verifyNoInteractions(repository);
    }

    private void authenticate() {
        when(jwtService.extractUserId("valid-token")).thenReturn(42L);
        when(jwtService.extractEmail("valid-token")).thenReturn("owner@example.test");
        when(jwtService.extractRole("valid-token")).thenReturn("USER");
    }
}
