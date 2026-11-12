package com.brandon.finance.expense.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.expense.dto.ExpenseDTO;
import com.brandon.finance.expense.dto.ExpenseFilterRequest;
import com.brandon.finance.expense.entity.Expense;
import com.brandon.finance.expense.enums.PeriodType;
import com.brandon.finance.expense.mapper.ExpenseMapper;
import com.brandon.finance.expense.repository.ExpenseRepository;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    @Mock com.brandon.finance.category.repository.CategoryRepository categories;
    @Mock com.brandon.finance.account.repository.AccountRepository accounts;
    @Mock ExpenseRepository repository;
    @Mock ExpenseMapper mapper;
    @Mock AuthenticatedUserService authenticatedUserService;
    @InjectMocks ExpenseService service;

    @Test
    void updatesAllFieldsAndPreservesOwnerThenRemovesOptionalLinks() {
        var owner = new com.brandon.finance.user.entity.User(42L);
        var expense = new Expense();
        expense.setUser(owner);
        var category = new com.brandon.finance.category.entity.Category(2L);
        var account = new com.brandon.finance.account.entity.Account(3L);
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        when(repository.findByIdAndUserId(1L, 42L)).thenReturn(java.util.Optional.of(expense));
        when(categories.findByIdAndUserId(2L, 42L)).thenReturn(java.util.Optional.of(category));
        when(accounts.findByIdAndUserId(3L, 42L)).thenReturn(java.util.Optional.of(account));
        when(repository.save(expense)).thenReturn(expense);
        when(mapper.toDTO(expense)).thenAnswer(invocation -> new ExpenseMapper().toDTO(expense));
        var dto = new ExpenseDTO(999L, "Café", new BigDecimal("12.34"), LocalDate.of(2026, 3, 15), 999L, 2L, 3L, null);

        var result = service.update(1L, dto);

        assertThat(result.getDescription()).isEqualTo("Café");
        assertThat(result.getAmount()).isEqualByComparingTo("12.34");
        assertThat(result.getDate()).isEqualTo(dto.getDate());
        assertThat(result.getCategoryId()).isEqualTo(2L);
        assertThat(result.getAccountId()).isEqualTo(3L);
        assertThat(expense.getUser()).isSameAs(owner);
        dto.setCategoryId(null);
        dto.setAccountId(null);
        service.update(1L, dto);
        assertThat(expense.getCategory()).isNull();
        assertThat(expense.getAccount()).isNull();
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = {true, false})
    void rejectsForeignLinksBeforeMutatingExpense(boolean foreignCategory) {
        var expense = new Expense();
        expense.setDescription("Original");
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        when(repository.findByIdAndUserId(1L, 42L)).thenReturn(java.util.Optional.of(expense));
        var dto = new ExpenseDTO();
        dto.setDescription("Alterada");
        if (foreignCategory) dto.setCategoryId(99L); else dto.setAccountId(99L);
        assertThatThrownBy(() -> service.update(1L, dto))
            .isInstanceOf(com.brandon.finance.shared.base.excepetion.ResourceNotFoundException.class);
        assertThat(expense.getDescription()).isEqualTo("Original");
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void foreignExpenseCannotBeReadUpdatedOrDeleted() {
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        for (Runnable operation : List.<Runnable>of(() -> service.getById(99L),
                () -> service.update(99L, new ExpenseDTO()), () -> service.delete(99L))) {
            assertThatThrownBy(operation::run)
                .isInstanceOf(com.brandon.finance.shared.base.excepetion.ResourceNotFoundException.class);
        }
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).delete(org.mockito.ArgumentMatchers.any(Expense.class));
    }

    @Test
    void mapsPageWithoutLosingPaginationOrSorting() {
        var pageable = PageRequest.of(1, 2, Sort.by("date").descending());
        Expense entity = new Expense();
        ExpenseDTO dto = new ExpenseDTO();
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<Expense>>any(), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(entity), pageable, 3));
        when(mapper.toDTO(entity)).thenReturn(dto);

        Page<ExpenseDTO> result = service.filter(new ExpenseFilterRequest(), pageable);

        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getSort()).isEqualTo(pageable.getSort());
    }

    @Test
    void rejectsCustomPeriodWithoutBothDates() {
        var filter = new ExpenseFilterRequest();
        filter.setPeriodType(PeriodType.CUSTOM);
        filter.setStartDate(LocalDate.of(2026, 1, 1));

        assertInvalid(filter);
    }

    @Test
    void rejectsReversedDateRange() {
        var filter = new ExpenseFilterRequest();
        filter.setStartDate(LocalDate.of(2026, 1, 2));
        filter.setEndDate(LocalDate.of(2026, 1, 1));

        assertInvalid(filter);
    }

    @Test
    void rejectsReversedAmountRange() {
        var filter = new ExpenseFilterRequest();
        filter.setMinAmount(new BigDecimal("20"));
        filter.setMaxAmount(new BigDecimal("10"));

        assertInvalid(filter);
    }

    private void assertInvalid(ExpenseFilterRequest filter) {
        assertThatThrownBy(() -> service.filter(filter, PageRequest.of(0, 20)))
            .isInstanceOfSatisfying(BusinessException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
        verifyNoInteractions(repository);
    }
}
