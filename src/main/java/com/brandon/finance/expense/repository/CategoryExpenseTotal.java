package com.brandon.finance.expense.repository;

import java.math.BigDecimal;

/** Projeção agregada: evita carregar despesas e associações para montar o resumo. */
public interface CategoryExpenseTotal {
    Long getCategoryId();
    String getCategoryName();
    BigDecimal getAmount();
}
