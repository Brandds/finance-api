package com.brandon.finance.expense_tag.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ExpenseTagId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long expenseId;
    private Long tagId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpenseTagId that = (ExpenseTagId) o;
        return Objects.equals(expenseId, that.expenseId) &&
               Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expenseId, tagId);
    }
}
