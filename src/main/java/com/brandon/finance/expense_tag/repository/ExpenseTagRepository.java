package com.brandon.finance.expense_tag.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brandon.finance.expense_tag.entity.ExpenseTag;
import com.brandon.finance.expense_tag.entity.ExpenseTagId;

@Repository
public interface ExpenseTagRepository extends JpaRepository<ExpenseTag, ExpenseTagId> {

    List<ExpenseTag> findByIdExpenseIdAndExpenseUserIdAndTagUserId(Long expenseId, Long expenseUserId, Long tagUserId);

    Page<ExpenseTag> findByIdExpenseIdAndExpenseUserIdAndTagUserId(Long expenseId, Long expenseUserId, Long tagUserId, Pageable pageable);

    List<ExpenseTag> findByIdTagIdAndExpenseUserIdAndTagUserId(Long tagId, Long expenseUserId, Long tagUserId);

    Page<ExpenseTag> findByIdTagIdAndExpenseUserIdAndTagUserId(Long tagId, Long expenseUserId, Long tagUserId, Pageable pageable);

    Optional<ExpenseTag> findByIdAndExpenseUserIdAndTagUserId(ExpenseTagId id, Long expenseUserId, Long tagUserId);
}
