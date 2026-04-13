package com.brandon.finance.budget.mapper;

import org.springframework.stereotype.Component;

import com.brandon.finance.budget.dto.BudgetDTO;
import com.brandon.finance.budget.entity.Budget;
import com.brandon.finance.category.entity.Category;
import com.brandon.finance.user.entity.User;

@Component
public class BudgetMapper {

    public BudgetDTO toDTO(Budget budget) {
        if (budget == null) {
            return null;
        }

        return new BudgetDTO(
            budget.getId(),
            budget.getUser() != null ? budget.getUser().getId() : null,
            budget.getCategory() != null ? budget.getCategory().getId() : null,
            budget.getLimitAmount(),
            budget.getMonth(),
            budget.getYear()
        );
    }

    public Budget toEntity(BudgetDTO dto) {
        if (dto == null) {
            return null;
        }

        Budget budget = new Budget();
        budget.setLimitAmount(dto.getLimitAmount());
        budget.setMonth(dto.getMonth());
        budget.setYear(dto.getYear());
        budget.setUser(new User(dto.getUserId()));
        budget.setCategory(new Category(dto.getCategoryId()));

        return budget;
    }
}
