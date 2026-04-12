package com.brandon.finance.expense.mapper;

import org.springframework.stereotype.Component;

import com.brandon.finance.account.entity.Account;
import com.brandon.finance.category.entity.Category;
import com.brandon.finance.expense.dto.ExpenseDTO;
import com.brandon.finance.expense.entity.Expense;
import com.brandon.finance.user.entity.User;

@Component
public class ExpenseMapper {

    public ExpenseDTO toDTO(Expense expense) {
        if (expense == null) {
            return null;
        }

        return new ExpenseDTO(
            expense.getId(),
            expense.getDescription(),
            expense.getAmount(),
            expense.getDate(),
            expense.getUser() != null ? expense.getUser().getId() : null,
            expense.getCategory() != null ? expense.getCategory().getId() : null,
            expense.getAccount() != null ? expense.getAccount().getId() : null
        );
    }

    public Expense toEntity(ExpenseDTO dto) {
        if (dto == null) {
            return null;
        }

        Expense expense = new Expense();
        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setDate(dto.getDate());
        expense.setUser(dto.getUserId() != null ? new User(dto.getUserId()) : null);
        expense.setCategory(dto.getCategoryId() != null ? new Category(dto.getCategoryId()) : null);
        expense.setAccount(dto.getAccountId() != null ? new Account(dto.getAccountId()) : null);
        return expense;
    }
}
