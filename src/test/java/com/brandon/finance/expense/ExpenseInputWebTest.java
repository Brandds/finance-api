package com.brandon.finance.expense;

import com.brandon.finance.expense.controller.ExpenseController;
import com.brandon.finance.expense.service.ExpenseService;
import com.brandon.finance.shared.base.excepetion.GlobalExceptionHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import java.util.stream.Stream;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExpenseInputWebTest {
    @org.junit.jupiter.api.Test
    void trimsDescriptionAndBindsCalendarDateAndNullLinks() throws Exception {
        ExpenseService service = mock(ExpenseService.class);
        when(service.update(org.mockito.ArgumentMatchers.eq(1L), any()))
            .thenAnswer(invocation -> invocation.getArgument(1));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ExpenseController(service))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(put("/api/v1/expenses/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"  Café  \",\"amount\":0.01,\"date\":\"2024-02-29\",\"categoryId\":null,\"accountId\":null}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.description").value("Café"));
        var captor = org.mockito.ArgumentCaptor.forClass(com.brandon.finance.expense.dto.ExpenseDTO.class);
        verify(service).update(org.mockito.ArgumentMatchers.eq(1L), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getCategoryId()).isNull();
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getAccountId()).isNull();
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getDate()).isEqualTo(java.time.LocalDate.of(2024, 2, 29));
    }

    static Stream<String> invalidBodies() {
        String valid = "{\"description\":\"Café\",\"amount\":12.34,\"date\":\"2026-03-15\"}";
        return Stream.of(
            valid.replace("Café", "   "), valid.replace("Café", "x".repeat(256)),
            valid.replace("12.34", "null"), valid.replace("12.34", "0"),
            valid.replace("12.34", "-1"), valid.replace("12.34", "1.234"),
            valid.replace("\"2026-03-15\"", "null"), valid.replace("2026-03-15", "2026-02-30"),
            valid.replace("2026-03-15", "invalid"));
    }

    @ParameterizedTest
    @MethodSource("invalidBodies")
    void rejectsInvalidCreationAndUpdateBeforeCallingService(String body) throws Exception {
        ExpenseService service = mock(ExpenseService.class);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ExpenseController(service))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
        for (var request : new org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder[]{
                post("/api/v1/expenses"), put("/api/v1/expenses/1")}) {
            mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
        }
        verifyNoInteractions(service);
    }
}
