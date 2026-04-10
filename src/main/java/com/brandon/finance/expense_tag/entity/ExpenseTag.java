package com.brandon.finance.expense_tag.entity;

import com.brandon.finance.expense.entity.Expense;
import com.brandon.finance.tag.entity.Tag;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "expense_tag")
public class ExpenseTag {

    @EmbeddedId
    private ExpenseTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("expenseId")
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
