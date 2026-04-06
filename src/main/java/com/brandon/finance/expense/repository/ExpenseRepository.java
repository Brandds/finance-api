package com.brandon.finance.expense.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.brandon.finance.expense.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense,Long> {
    
}
