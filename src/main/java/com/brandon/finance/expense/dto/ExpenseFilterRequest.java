package com.brandon.finance.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.brandon.finance.expense.enums.PeriodType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenseFilterRequest {
    private List<@Positive Long> categoryIds;
    private PeriodType periodType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @DecimalMin("0.0")
    private BigDecimal minAmount;

    @DecimalMin("0.0")
    private BigDecimal maxAmount;
}
