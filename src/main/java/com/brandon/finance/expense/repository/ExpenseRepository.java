package com.brandon.finance.expense.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.brandon.finance.expense.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense,Long> {

    Page<Expense> findByUserId(Long userId, Pageable pageable);

    Optional<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId);

    Page<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Optional<Expense> findByUserIdAndAccountId(Long userId, Long accountId);
    
}
