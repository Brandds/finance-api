package com.brandon.finance.expense_tag.mapper;

import org.springframework.stereotype.Component;

import com.brandon.finance.expense_tag.dto.ExpenseTagDTO;
import com.brandon.finance.expense_tag.entity.ExpenseTag;
import com.brandon.finance.expense_tag.entity.ExpenseTagId;

@Component
public class ExpenseTagMapper {

    public ExpenseTagDTO toDTO(ExpenseTag expenseTag) {
        if (expenseTag == null) {
            return null;
        }

        return new ExpenseTagDTO(
            expenseTag.getId().getExpenseId(),
            expenseTag.getId().getTagId()
        );
    }

    public ExpenseTag toEntity(ExpenseTagDTO dto) {
        if (dto == null) {
            return null;
        }

        ExpenseTag expenseTag = new ExpenseTag();
        ExpenseTagId id = new ExpenseTagId(dto.getExpenseId(), dto.getTagId());
        expenseTag.setId(id);

        return expenseTag;
    }
}
