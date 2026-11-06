package com.brandon.finance.expense.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.brandon.finance.expense.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense,Long>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    @Query("""
        select c.id as categoryId, c.name as categoryName, sum(e.amount) as amount
        from Expense e left join e.category c on c.user.id = :userId
        where e.user.id = :userId and e.date between :startDate and :endDate
        group by c.id, c.name
        """)
    List<CategoryExpenseTotal> summarizeByCategory(@Param("userId") Long userId,
        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
        select coalesce(sum(e.amount), 0) from Expense e
        where e.user.id = :userId and e.date between :startDate and :endDate
        """)
    BigDecimal sumByUserAndPeriod(@Param("userId") Long userId,
        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    Page<Expense> findByUserId(Long userId, Pageable pageable);

    Optional<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId);

    Page<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    Optional<Expense> findByUserIdAndAccountId(Long userId, Long accountId);
    
}
