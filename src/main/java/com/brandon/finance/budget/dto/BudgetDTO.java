package com.brandon.finance.budget.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {

    private Long id;
    private Long userId;
    private Long categoryId;
    private BigDecimal limitAmount;
    private Integer month;
    private Integer year;
}
