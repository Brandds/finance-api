package com.brandon.finance.expense.dto;

import java.math.BigDecimal;

/** Categoria com maior gasto no período, disponível apenas quando o total é positivo. */
public record ExpenseInsightDTO(String type, Long categoryId, String categoryName, BigDecimal amount,
        BigDecimal percentage) {}
