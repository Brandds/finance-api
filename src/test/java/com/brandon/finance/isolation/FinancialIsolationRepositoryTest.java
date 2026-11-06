package com.brandon.finance.isolation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import com.brandon.finance.account.entity.Account;
import com.brandon.finance.account.repository.AccountRepository;
import com.brandon.finance.budget.entity.Budget;
import com.brandon.finance.budget.repository.BudgetRepository;
import com.brandon.finance.category.entity.Category;
import com.brandon.finance.category.enums.CategoryIconEnum;
import com.brandon.finance.category.repository.CategoryRepository;
import com.brandon.finance.expense.entity.Expense;
import com.brandon.finance.expense.repository.ExpenseRepository;
import com.brandon.finance.expense_tag.entity.ExpenseTag;
import com.brandon.finance.expense_tag.entity.ExpenseTagId;
import com.brandon.finance.expense_tag.repository.ExpenseTagRepository;
import com.brandon.finance.financial_transaction.entity.FinancialTransaction;
import com.brandon.finance.financial_transaction.repository.FinancialTransactionRepository;
import com.brandon.finance.tag.entity.Tag;
import com.brandon.finance.tag.repository.TagRepository;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.enums.Role;

/** Executar somente em PostgreSQL descartável chamado finance_contracts_test. */
@EnabledIfEnvironmentVariable(named = "TEST_DATABASE_URL", matches = "jdbc:postgresql://.*/finance_contracts_test")
@DataJpaTest(showSql = false, properties = {
    "spring.datasource.url=${TEST_DATABASE_URL}",
    "spring.datasource.username=contracts_test",
    "spring.datasource.password=test-only-password",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true",
    "spring.liquibase.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = FinancialIsolationRepositoryTest.JpaConfig.class)
class FinancialIsolationRepositoryTest {
    @Configuration
    @EntityScan("com.brandon.finance")
    @EnableJpaRepositories(basePackageClasses = {AccountRepository.class, CategoryRepository.class,
        ExpenseRepository.class, BudgetRepository.class, FinancialTransactionRepository.class,
        TagRepository.class, ExpenseTagRepository.class})
    @EnableJpaAuditing
    static class JpaConfig {}

    @Autowired TestEntityManager em;
    @Autowired AccountRepository accounts;
    @Autowired CategoryRepository categories;
    @Autowired ExpenseRepository expenses;
    @Autowired BudgetRepository budgets;
    @Autowired FinancialTransactionRepository transactions;
    @Autowired TagRepository tags;
    @Autowired ExpenseTagRepository links;

    @Test
    void scopedIdQueriesFindOwnersRecordsAndHideThemFromOtherUsersForEveryResource() {
        var owner = user("owner", "11111111111");
        var other = user("other", "22222222222");
        var category = category(owner);
        var account = new Account();
        account.setName("Private account");
        account.setType(Account.AccountType.CASH);
        account.setUser(owner);
        em.persist(account);
        var expense = expense(owner);
        var budget = new Budget();
        budget.setUser(owner);
        budget.setCategory(category);
        budget.setLimitAmount(new BigDecimal("100"));
        budget.setMonth(1);
        budget.setYear(2026);
        em.persist(budget);
        var transaction = new FinancialTransaction();
        transaction.setUser(owner);
        transaction.setAmount(BigDecimal.TEN);
        transaction.setDate(LocalDate.of(2026, 1, 1));
        transaction.setType(FinancialTransaction.TransactionType.EXPENSE);
        em.persist(transaction);
        var tag = tag(owner);
        em.flush();
        em.clear();

        assertThat(accounts.findByIdAndUserId(account.getId(), owner.getId())).isPresent();
        assertThat(accounts.findByIdAndUserId(account.getId(), other.getId())).isEmpty();
        assertThat(categories.findByIdAndUserId(category.getId(), owner.getId())).isPresent();
        assertThat(categories.findByIdAndUserId(category.getId(), other.getId())).isEmpty();
        assertThat(expenses.findByIdAndUserId(expense.getId(), owner.getId())).isPresent();
        assertThat(expenses.findByIdAndUserId(expense.getId(), other.getId())).isEmpty();
        assertThat(budgets.findByIdAndUserId(budget.getId(), owner.getId())).isPresent();
        assertThat(budgets.findByIdAndUserId(budget.getId(), other.getId())).isEmpty();
        assertThat(transactions.findByIdAndUserId(transaction.getId(), owner.getId())).isPresent();
        assertThat(transactions.findByIdAndUserId(transaction.getId(), other.getId())).isEmpty();
        assertThat(tags.findByIdAndUserId(tag.getId(), owner.getId())).isPresent();
        assertThat(tags.findByIdAndUserId(tag.getId(), other.getId())).isEmpty();
    }

    @Test
    void linkQueriesExcludeMixedOwnerLegacyLinksIncludingPaginationAndLookupForDeletion() {
        var owner = user("owner", "11111111111");
        var other = user("other", "22222222222");
        var ownExpense = expense(owner);
        var otherExpense = expense(other);
        var ownTag = tag(owner);
        var otherTag = tag(other);
        var owned = link(ownExpense, ownTag);
        var foreignTag = link(ownExpense, otherTag);
        var foreignExpense = link(otherExpense, ownTag);
        link(otherExpense, otherTag);
        em.flush();
        em.clear();
        Long id = owner.getId();
        var pageable = PageRequest.of(0, 10);

        assertThat(links.findByIdExpenseIdAndExpenseUserIdAndTagUserId(ownExpense.getId(), id, id))
            .extracting(ExpenseTag::getId).containsExactly(owned.getId());
        assertThat(links.findByIdTagIdAndExpenseUserIdAndTagUserId(ownTag.getId(), id, id))
            .extracting(ExpenseTag::getId).containsExactly(owned.getId());
        var byExpense = links.findByIdExpenseIdAndExpenseUserIdAndTagUserId(ownExpense.getId(), id, id, pageable);
        var byTag = links.findByIdTagIdAndExpenseUserIdAndTagUserId(ownTag.getId(), id, id, pageable);
        assertThat(byExpense.getTotalElements()).isEqualTo(1);
        assertThat(byTag.getTotalElements()).isEqualTo(1);
        assertThat(links.findByIdAndExpenseUserIdAndTagUserId(owned.getId(), id, id)).isPresent();
        assertThat(links.findByIdAndExpenseUserIdAndTagUserId(foreignTag.getId(), id, id)).isEmpty();
        assertThat(links.findByIdAndExpenseUserIdAndTagUserId(foreignExpense.getId(), id, id)).isEmpty();
    }

    private User user(String name, String cpf) {
        return em.persist(new User(name, name + "@example.test", "hash", Role.USER, cpf));
    }

    private Category category(User user) {
        var category = new Category();
        category.setUser(user);
        category.setName("Private category");
        category.setIcon(CategoryIconEnum.FOOD);
        return em.persist(category);
    }

    private Expense expense(User user) {
        var expense = new Expense();
        expense.setUser(user);
        expense.setDescription("Private expense");
        expense.setAmount(BigDecimal.TEN);
        expense.setDate(LocalDate.of(2026, 1, 1));
        return em.persist(expense);
    }

    private Tag tag(User user) {
        var tag = new Tag();
        tag.setUser(user);
        tag.setName("Private tag");
        return em.persist(tag);
    }

    private ExpenseTag link(Expense expense, Tag tag) {
        var link = new ExpenseTag();
        link.setId(new ExpenseTagId(expense.getId(), tag.getId()));
        link.setExpense(expense);
        link.setTag(tag);
        return em.persist(link);
    }
}
