package com.brandon.finance.expense_tag.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseTagDTO {

    private Long expenseId;
    private Long tagId;
}
