package com.brandon.finance.financial_transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.brandon.finance.financial_transaction.entity.FinancialTransaction.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinancialTransactionDTO {

    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private TransactionType type;
    private Long categoryId;
    private Long accountId;
    private Long userId;
}
