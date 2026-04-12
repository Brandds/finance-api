package com.brandon.finance.expense_tag.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brandon.finance.expense_tag.entity.ExpenseTag;
import com.brandon.finance.expense_tag.entity.ExpenseTagId;

@Repository
public interface ExpenseTagRepository extends JpaRepository<ExpenseTag, ExpenseTagId> {

    List<ExpenseTag> findByIdExpenseId(Long expenseId);

    Page<ExpenseTag> findByIdExpenseId(Long expenseId, Pageable pageable);

    List<ExpenseTag> findByIdTagId(Long tagId);

    Page<ExpenseTag> findByIdTagId(Long tagId, Pageable pageable);
}
