package com.brandon.finance.expense.dto;

import java.math.BigDecimal;

public record ExpenseCategorySummaryDTO(Long categoryId, String categoryName, BigDecimal amount,
        BigDecimal percentage) {}
