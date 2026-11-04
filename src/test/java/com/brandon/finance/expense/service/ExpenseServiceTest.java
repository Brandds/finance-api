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
    @Mock ExpenseRepository repository;
    @Mock ExpenseMapper mapper;
    @Mock AuthenticatedUserService authenticatedUserService;
    @InjectMocks ExpenseService service;

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
