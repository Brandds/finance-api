package com.brandon.finance.expense.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.brandon.finance.expense.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense,Long>, JpaSpecificationExecutor<Expense> {

    Page<Expense> findByUserId(Long userId, Pageable pageable);

    Optional<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId);

    Page<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    Optional<Expense> findByUserIdAndAccountId(Long userId, Long accountId);
    
}
