package com.brandon.finance.isolation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.brandon.finance.account.dto.AccountDTO;
import com.brandon.finance.account.mapper.AccountMapper;
import com.brandon.finance.account.repository.AccountRepository;
import com.brandon.finance.account.service.AccountService;
import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.budget.dto.BudgetDTO;
import com.brandon.finance.budget.mapper.BudgetMapper;
import com.brandon.finance.budget.repository.BudgetRepository;
import com.brandon.finance.budget.service.BudgetService;
import com.brandon.finance.category.dto.CategoryDTO;
import com.brandon.finance.category.mapper.CategoryMapper;
import com.brandon.finance.category.repository.CategoryRepository;
import com.brandon.finance.category.service.CategoryService;
import com.brandon.finance.expense.dto.ExpenseDTO;
import com.brandon.finance.expense.entity.Expense;
import com.brandon.finance.expense.mapper.ExpenseMapper;
import com.brandon.finance.expense.repository.ExpenseRepository;
import com.brandon.finance.expense.service.ExpenseService;
import com.brandon.finance.financial_transaction.dto.FinancialTransactionDTO;
import com.brandon.finance.financial_transaction.mapper.FinancialTransactionMapper;
import com.brandon.finance.financial_transaction.repository.FinancialTransactionRepository;
import com.brandon.finance.financial_transaction.service.FinancialTransactionService;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.tag.dto.TagDTO;
import com.brandon.finance.tag.mapper.TagMapper;
import com.brandon.finance.tag.repository.TagRepository;
import com.brandon.finance.tag.service.TagService;

@ExtendWith(MockitoExtension.class)
class FinancialResourceIsolationTest {
    enum Resource { ACCOUNT, CATEGORY, EXPENSE, BUDGET, TRANSACTION, TAG }

    @Mock AccountRepository accounts;
    @Mock CategoryRepository categories;
    @Mock ExpenseRepository expenses;
    @Mock BudgetRepository budgets;
    @Mock FinancialTransactionRepository transactions;
    @Mock TagRepository tags;
    @Mock AuthenticatedUserService authenticatedUser;

    @BeforeEach
    void authenticate() {
        when(authenticatedUser.getUserId()).thenReturn(42L);
    }

    @ParameterizedTest
    @EnumSource(Resource.class)
    void rejectsReadUpdateAndDeleteWhenIdDoesNotBelongToAuthenticatedUser(Resource resource) {
        // ID 7 existe para outro usuário, mas a consulta restrita ao usuário 42 não o encontra.
        for (Runnable operation : operations(resource)) {
            assertThatThrownBy(operation::run).isInstanceOf(ResourceNotFoundException.class);
        }
        switch (resource) {
            case ACCOUNT -> verify(accounts, times(3)).findByIdAndUserId(7L, 42L);
            case CATEGORY -> verify(categories, times(3)).findByIdAndUserId(7L, 42L);
            case EXPENSE -> verify(expenses, times(3)).findByIdAndUserId(7L, 42L);
            case BUDGET -> verify(budgets, times(3)).findByIdAndUserId(7L, 42L);
            case TRANSACTION -> verify(transactions, times(3)).findByIdAndUserId(7L, 42L);
            case TAG -> verify(tags, times(3)).findByIdAndUserId(7L, 42L);
        }
        verify(accounts, never()).save(any());
        verify(accounts, never()).delete(any());
        verify(categories, never()).save(any());
        verify(categories, never()).delete(any());
        verify(expenses, never()).save(any());
        verify(expenses, never()).delete(any(Expense.class));
        verify(budgets, never()).save(any());
        verify(budgets, never()).delete(any());
        verify(transactions, never()).save(any());
        verify(transactions, never()).delete(any());
        verify(tags, never()).save(any());
        verify(tags, never()).delete(any());
    }

    private List<Runnable> operations(Resource resource) {
        return switch (resource) {
            case ACCOUNT -> {
                var service = new AccountService(accounts, new AccountMapper(), authenticatedUser);
                yield List.of(() -> service.getById(7L), () -> service.update(7L, new AccountDTO()), () -> service.delete(7L));
            }
            case CATEGORY -> {
                var service = new CategoryService(categories, new CategoryMapper(), authenticatedUser);
                yield List.of(() -> service.getById(7L), () -> service.update(7L, new CategoryDTO()), () -> service.delete(7L));
            }
            case EXPENSE -> {
                var service = new ExpenseService(expenses, new ExpenseMapper(), authenticatedUser, categories, accounts);
                yield List.of(() -> service.getById(7L), () -> service.update(7L, new ExpenseDTO()), () -> service.delete(7L));
            }
            case BUDGET -> {
                var service = new BudgetService(budgets, new BudgetMapper(), authenticatedUser, categories);
                yield List.of(() -> service.getById(7L), () -> service.update(7L, new BudgetDTO()), () -> service.delete(7L));
            }
            case TRANSACTION -> {
                var service = new FinancialTransactionService(transactions, new FinancialTransactionMapper(), authenticatedUser, categories, accounts);
                yield List.of(() -> service.getById(7L), () -> service.update(7L, new FinancialTransactionDTO()), () -> service.delete(7L));
            }
            case TAG -> {
                var service = new TagService(tags, new TagMapper(), authenticatedUser);
                yield List.of(() -> service.getById(7L), () -> service.update(7L, new TagDTO()), () -> service.delete(7L));
            }
        };
    }
}
