package com.brandon.finance.expense.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.expense.dto.ExpenseCategorySummaryDTO;
import com.brandon.finance.expense.mapper.ExpenseMapper;
import com.brandon.finance.expense.repository.CategoryExpenseTotal;
import com.brandon.finance.expense.repository.ExpenseRepository;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;

@ExtendWith(MockitoExtension.class)
class ExpenseSummaryServiceTest {
    private static final LocalDate START = LocalDate.of(2026, 3, 1);
    private static final LocalDate END = LocalDate.of(2026, 3, 31);
    @Mock ExpenseRepository repository;
    @Mock ExpenseMapper mapper;
    @Mock AuthenticatedUserService authenticatedUserService;
    @InjectMocks ExpenseService service;

    @Test
    void calculatesTotalsDistributionAndDataBasedInsightForAuthenticatedUser() {
        prepare(List.of(total(2L, "Transporte", "25.50"), total(1L, "Alimentação", "64.50"),
            total(null, null, "10")), "80");

        var result = service.getExpenseAnalysis(START, END);

        assertThat(result.getTotalSpent()).isEqualByComparingTo("100");
        assertThat(result.getPreviousPeriodTotal()).isEqualByComparingTo("80");
        assertThat(result.getPercentageChange()).isEqualByComparingTo("25");
        assertThat(result.getCategories()).containsExactly(
            new ExpenseCategorySummaryDTO(1L, "Alimentação", new BigDecimal("64.50"), new BigDecimal("64.50")),
            new ExpenseCategorySummaryDTO(2L, "Transporte", new BigDecimal("25.50"), new BigDecimal("25.50")),
            new ExpenseCategorySummaryDTO(null, "Sem categoria", new BigDecimal("10"), new BigDecimal("10.00")));
        assertThat(result.getInsight().type()).isEqualTo("LARGEST_CATEGORY");
        assertThat(result.getInsight().categoryId()).isEqualTo(1L);
        assertThat(result.getInsight().amount()).isEqualByComparingTo("64.50");
        assertThat(result.getInsight().percentage()).isEqualByComparingTo("64.50");
        verify(repository).summarizeByCategory(42L, START, END);
        verify(repository).sumByUserAndPeriod(42L, LocalDate.of(2026, 1, 29), LocalDate.of(2026, 2, 28));
    }

    @ParameterizedTest
    @CsvSource({
        "2026-03-01,2026-03-31,2026-01-29,2026-02-28",
        "2024-03-01,2024-03-31,2024-01-30,2024-02-29",
        "2026-01-01,2026-01-01,2025-12-31,2025-12-31",
        "2026-01-01,2026-01-07,2025-12-25,2025-12-31"
    })
    void comparesWithImmediatelyPrecedingIntervalOfEqualInclusiveDuration(
            LocalDate start, LocalDate end, LocalDate previousStart, LocalDate previousEnd) {
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        when(repository.summarizeByCategory(42L, start, end)).thenReturn(List.of());
        when(repository.sumByUserAndPeriod(42L, previousStart, previousEnd)).thenReturn(BigDecimal.ZERO);

        var result = service.getExpenseAnalysis(start, end);

        assertThat(result.getStartDate()).isEqualTo(start);
        assertThat(result.getEndDate()).isEqualTo(end);
        assertThat(result.getPreviousStartDate()).isEqualTo(previousStart);
        assertThat(result.getPreviousEndDate()).isEqualTo(previousEnd);
    }

    @Test
    void percentagesSumToApproximatelyOneHundredWithRepeatingDecimals() {
        prepare(List.of(total(3L, "C", "1"), total(2L, "B", "1"), total(1L, "A", "1")), "0");

        var result = service.getExpenseAnalysis(START, END);

        assertThat(result.getCategories().stream().map(ExpenseCategorySummaryDTO::percentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add)).isCloseTo(new BigDecimal("100"), within(new BigDecimal("0.02")));
        assertThat(result.getCategories()).extracting(ExpenseCategorySummaryDTO::categoryName).containsExactly("A", "B", "C");
        assertThat(result.getInsight().categoryId()).isEqualTo(1L);
        assertThat(result.getPercentageChange()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"0,0", "50,-100"})
    void handlesNoExpensesInCurrentPeriod(String previous, String change) {
        prepare(List.of(), previous);

        var result = service.getExpenseAnalysis(START, END);

        assertThat(result.getTotalSpent()).isZero();
        assertThat(result.getPercentageChange()).isEqualByComparingTo(change);
        assertThat(result.getCategories()).isEmpty();
        assertThat(result.getInsight()).isNull();
    }

    @Test
    void doesNotInventGrowthWhenThereIsNoPreviousBase() {
        prepare(List.of(total(null, null, "10")), "0");

        var result = service.getExpenseAnalysis(START, END);

        assertThat(result.getPercentageChange()).isZero();
        assertThat(result.getCategories().getFirst().percentage()).isEqualByComparingTo("100");
        assertThat(result.getInsight().categoryName()).isEqualTo("Sem categoria");
    }

    @Test
    void handlesZeroValuedExpensesWithoutDivisionByZero() {
        prepare(List.of(total(1L, "Alimentação", "0")), "0");

        var result = service.getExpenseAnalysis(START, END);

        assertThat(result.getCategories().getFirst().percentage()).isZero();
        assertThat(result.getInsight()).isNull();
    }

    @Test
    void rejectsInvalidOrUnrepresentablePeriodsBeforeAccessingData() {
        for (LocalDate[] dates : List.of(new LocalDate[]{null, END}, new LocalDate[]{START, null},
                new LocalDate[]{END, START}, new LocalDate[]{LocalDate.MIN, LocalDate.MIN})) {
            assertThatThrownBy(() -> service.getExpenseAnalysis(dates[0], dates[1]))
                .isInstanceOfSatisfying(BusinessException.class,
                    error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
        }
        verifyNoInteractions(repository, authenticatedUserService);
    }

    private void prepare(List<CategoryExpenseTotal> totals, String previous) {
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        when(repository.summarizeByCategory(42L, START, END)).thenReturn(totals);
        when(repository.sumByUserAndPeriod(42L, LocalDate.of(2026, 1, 29), LocalDate.of(2026, 2, 28)))
            .thenReturn(new BigDecimal(previous));
    }

    private CategoryExpenseTotal total(Long id, String name, String amount) {
        return new CategoryExpenseTotal() {
            public Long getCategoryId() { return id; }
            public String getCategoryName() { return name; }
            public BigDecimal getAmount() { return new BigDecimal(amount); }
        };
    }
}
