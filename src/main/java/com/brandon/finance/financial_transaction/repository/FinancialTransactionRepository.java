package com.brandon.finance.financial_transaction.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brandon.finance.financial_transaction.entity.FinancialTransaction;
import com.brandon.finance.financial_transaction.entity.FinancialTransaction.TransactionType;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    List<FinancialTransaction> findByUserId(Long userId);

    Page<FinancialTransaction> findByUserId(Long userId, Pageable pageable);

    List<FinancialTransaction> findByUserIdAndType(Long userId, TransactionType type);

    Page<FinancialTransaction> findByUserIdAndType(Long userId, TransactionType type, Pageable pageable);

    List<FinancialTransaction> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<FinancialTransaction> findByUserIdAndAccountId(Long userId, Long accountId);

    List<FinancialTransaction> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    Page<FinancialTransaction> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
