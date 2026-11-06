package com.brandon.finance.isolation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.brandon.finance.account.entity.Account;
import com.brandon.finance.budget.entity.Budget;
import com.brandon.finance.budget.mapper.BudgetMapper;
import com.brandon.finance.category.entity.Category;
import com.brandon.finance.expense.entity.Expense;
import com.brandon.finance.expense.mapper.ExpenseMapper;
import com.brandon.finance.financial_transaction.entity.FinancialTransaction;
import com.brandon.finance.financial_transaction.mapper.FinancialTransactionMapper;
import com.brandon.finance.user.entity.User;

class LegacyReferenceMapperTest {
    @ParameterizedTest
    @ValueSource(longs = {42L, 43L})
    void exposesAssociationIdsAndCategoryNameOnlyWhenOwnedBySameUser(long relatedOwnerId) {
        var owner = new User(42L);
        var relatedOwner = new User(relatedOwnerId);
        var category = new Category(7L);
        category.setName("Private category");
        category.setUser(relatedOwner);
        var account = new Account(8L);
        account.setUser(relatedOwner);
        var expense = new Expense();
        expense.setUser(owner);
        expense.setCategory(category);
        expense.setAccount(account);
        var budget = new Budget();
        budget.setUser(owner);
        budget.setCategory(category);
        var transaction = new FinancialTransaction();
        transaction.setUser(owner);
        transaction.setCategory(category);
        transaction.setAccount(account);

        Long expectedCategory = relatedOwnerId == 42L ? 7L : null;
        Long expectedAccount = relatedOwnerId == 42L ? 8L : null;
        String expectedName = relatedOwnerId == 42L ? "Private category" : null;
        var expenseMapper = new ExpenseMapper();
        var expenseDTO = expenseMapper.toDTO(expense);
        var transactionDTO = new FinancialTransactionMapper().toDTO(transaction);
        assertThat(expenseDTO.getCategoryId()).isEqualTo(expectedCategory);
        assertThat(expenseDTO.getAccountId()).isEqualTo(expectedAccount);
        assertThat(expenseDTO.getCategoryName()).isEqualTo(expectedName);
        assertThat(expenseMapper.toDTOByDateRanger(expense).getCategoryName()).isEqualTo(expectedName);
        assertThat(new BudgetMapper().toDTO(budget).getCategoryId()).isEqualTo(expectedCategory);
        assertThat(transactionDTO.getCategoryId()).isEqualTo(expectedCategory);
        assertThat(transactionDTO.getAccountId()).isEqualTo(expectedAccount);
    }
}
