package com.brandon.finance.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExpenseAnalysisDTO {

	private BigDecimal totalSpent;
	private BigDecimal previousPeriodTotal;
	private BigDecimal percentageChange;
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDate previousStartDate;
	private LocalDate previousEndDate;
	private List<ExpenseCategorySummaryDTO> categories;
	private ExpenseInsightDTO insight;
}
