package com.brandon.finance.expense.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExpenseAnalysisDTO {

	private BigDecimal totalSpent;
	private BigDecimal previousPeriodTotal;
	private BigDecimal percentageChange;
}
