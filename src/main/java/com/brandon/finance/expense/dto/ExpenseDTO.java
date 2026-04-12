package com.brandon.finance.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {

    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private Long userId;
    private Long categoryId;
    private Long accountId;
}
