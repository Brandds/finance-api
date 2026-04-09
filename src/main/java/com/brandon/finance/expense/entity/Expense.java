package com.brandon.finance.expense.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.brandon.finance.category.entity.Category;
import com.brandon.finance.shared.base.entity.AuditableEntity;
import com.brandon.finance.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Table(name = "expense")
@Entity
public class Expense extends AuditableEntity {
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
