package com.brandon.finance.financial_transaction.mapper;

import org.springframework.stereotype.Component;

import com.brandon.finance.financial_transaction.dto.FinancialTransactionDTO;
import com.brandon.finance.financial_transaction.entity.FinancialTransaction;

@Component
public class FinancialTransactionMapper {

    public FinancialTransactionDTO toDTO(FinancialTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        return new FinancialTransactionDTO(
            transaction.getId(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getDate(),
            transaction.getType(),
            transaction.getCategory() != null ? transaction.getCategory().getId() : null,
            transaction.getAccount() != null ? transaction.getAccount().getId() : null,
            transaction.getUser() != null ? transaction.getUser().getId() : null
        );
    }

    public FinancialTransaction toEntity(FinancialTransactionDTO dto) {
        if (dto == null) {
            return null;
        }

        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setDescription(dto.getDescription());
        transaction.setAmount(dto.getAmount());
        transaction.setDate(dto.getDate());
        transaction.setType(dto.getType());

        return transaction;
    }
}
