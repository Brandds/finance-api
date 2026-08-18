package com.brandon.finance.category.entity;


import com.brandon.finance.category.enums.CategoryIconEnum;
import com.brandon.finance.shared.base.entity.AuditableEntity;
import com.brandon.finance.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Table(name = "category")
@Entity
public class Category extends AuditableEntity {
    
    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "icon", nullable = false)
    private CategoryIconEnum icon;

    public Category(Long categoryId) {
        super(categoryId);
    }
}
