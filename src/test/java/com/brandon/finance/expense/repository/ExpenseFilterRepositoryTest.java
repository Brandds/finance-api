package com.brandon.finance.expense.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.category.entity.Category;
import com.brandon.finance.category.repository.CategoryRepository;
import com.brandon.finance.account.repository.AccountRepository;
import com.brandon.finance.category.enums.CategoryIconEnum;
import com.brandon.finance.expense.dto.ExpenseFilterRequest;
import com.brandon.finance.expense.dto.ExpenseCategorySummaryDTO;
import com.brandon.finance.expense.entity.Expense;
import com.brandon.finance.expense.enums.PeriodType;
import com.brandon.finance.expense.mapper.ExpenseMapper;
import com.brandon.finance.expense.service.ExpenseService;
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
@ContextConfiguration(classes = ExpenseFilterRepositoryTest.JpaConfig.class)
@Import({ExpenseService.class, ExpenseMapper.class})
class ExpenseFilterRepositoryTest {
    @Configuration
    @EntityScan("com.brandon.finance")
    @EnableJpaRepositories(basePackageClasses = {ExpenseRepository.class, CategoryRepository.class, AccountRepository.class})
    @EnableJpaAuditing
    static class JpaConfig {}

    @Autowired TestEntityManager entityManager;
    @Autowired ExpenseService service;
    @MockitoBean AuthenticatedUserService authenticatedUserService;
    private User owner;
    private User other;
    private Category food;
    private Category transport;

    @BeforeEach
    void setUp() {
        owner = entityManager.persist(new User("Owner", "owner@example.test", "hash", Role.USER, "11111111111"));
        other = entityManager.persist(new User("Other", "other@example.test", "hash", Role.USER, "22222222222"));
        food = category("Food", owner);
        transport = category("Transport", owner);
        when(authenticatedUserService.getUserId()).thenReturn(owner.getId());
    }

    @Test
    void combinesFiltersInclusivelyAndNeverReturnsAnotherUsersExpenses() {
        var start = LocalDate.of(2026, 1, 1);
        var end = LocalDate.of(2026, 1, 31);
        var low = expense(owner, food, start, "10.00");
        var high = expense(owner, transport, end, "20.00");
        expense(other, food, start, "10.00");
        expense(owner, food, start.minusDays(1), "10.00");
        expense(owner, food, end.plusDays(1), "10.00");
        expense(owner, food, start, "9.99");
        expense(owner, food, start, "20.01");
        expense(owner, null, start, "15.00");
        var filter = new ExpenseFilterRequest();
        filter.setCategoryIds(List.of(food.getId(), transport.getId()));
        filter.setPeriodType(PeriodType.CUSTOM);
        filter.setStartDate(start);
        filter.setEndDate(end);
        filter.setMinAmount(new BigDecimal("10.00"));
        filter.setMaxAmount(new BigDecimal("20.00"));
        entityManager.flush();
        entityManager.clear();

        var first = service.filter(filter, PageRequest.of(0, 1, Sort.by("amount")));
        var second = service.filter(filter, PageRequest.of(1, 1, Sort.by("amount")));

        assertThat(first.getTotalElements()).isEqualTo(2);
        assertThat(first.getTotalPages()).isEqualTo(2);
        assertThat(first.getContent()).extracting(dto -> dto.getId()).containsExactly(low.getId());
        assertThat(second.getContent()).extracting(dto -> dto.getId()).containsExactly(high.getId());
        assertThat(first.getContent().getFirst().getCategoryName()).isEqualTo("Food");
        assertThat(first.getContent().getFirst().getDescription()).isEqualTo("Test expense");
    }

    @Test
    void emptyFilterIncludesUncategorizedExpensesButOnlyForAuthenticatedUser() {
        var own = expense(owner, null, LocalDate.of(2026, 1, 1), "10");
        expense(other, null, LocalDate.of(2026, 1, 1), "10");

        var page = service.filter(new ExpenseFilterRequest(), PageRequest.of(0, 20));

        assertThat(page.getContent()).extracting(dto -> dto.getId()).containsExactly(own.getId());
    }

    @Test
    void acceptsOneSidedDateAndAmountBounds() {
        var first = expense(owner, food, LocalDate.of(2026, 1, 1), "10");
        var last = expense(owner, transport, LocalDate.of(2026, 2, 1), "20");
        var filter = new ExpenseFilterRequest();
        filter.setStartDate(LocalDate.of(2026, 2, 1));
        filter.setMinAmount(new BigDecimal("20"));
        assertThat(service.filter(filter, PageRequest.of(0, 20)).getContent())
            .extracting(dto -> dto.getId()).containsExactly(last.getId());

        filter.setStartDate(null);
        filter.setMinAmount(null);
        filter.setEndDate(LocalDate.of(2026, 1, 1));
        filter.setMaxAmount(new BigDecimal("10"));
        assertThat(service.filter(filter, PageRequest.of(0, 20)).getContent())
            .extracting(dto -> dto.getId()).containsExactly(first.getId());
    }

    @ParameterizedTest
    @EnumSource(value = PeriodType.class, names = {"TODAY", "LAST_7_DAYS", "CURRENT_MONTH"})
    void resolvesPresetPeriodsAndOverridesExplicitDates(PeriodType period) {
        var today = LocalDate.now();
        LocalDate start = switch (period) {
            case TODAY -> today;
            case LAST_7_DAYS -> today.minusDays(6);
            case CURRENT_MONTH -> today.withDayOfMonth(1);
            default -> throw new IllegalArgumentException();
        };
        LocalDate end = period == PeriodType.CURRENT_MONTH ? today.withDayOfMonth(today.lengthOfMonth()) : today;
        var lower = expense(owner, food, start, "10");
        var upper = expense(owner, food, end, "10");
        expense(owner, food, start.minusDays(1), "10");
        expense(owner, food, end.plusDays(1), "10");
        expense(other, food, today, "10");
        var filter = new ExpenseFilterRequest();
        filter.setPeriodType(period);
        filter.setStartDate(LocalDate.of(2000, 1, 1));
        filter.setEndDate(LocalDate.of(2000, 1, 2));

        assertThat(service.filter(filter, PageRequest.of(0, 20, Sort.by("id"))).getContent())
            .extracting(dto -> dto.getId()).containsExactly(lower.getId(), upper.getId());
    }

    @Test
    void summarizesOnlyOwnedExpensesWithInclusiveBoundsAndKeepsUncategorizedAmounts() {
        var start = LocalDate.of(2024, 3, 1);
        var end = LocalDate.of(2024, 3, 31);
        var foreignCategory = category("Private category", other);
        expense(owner, food, start, "60");
        expense(owner, food, end, "40");
        expense(owner, transport, start, "40");
        expense(owner, null, start, "10");
        // Uma referência legada a categoria alheia não deve revelar seu ID/nome.
        expense(owner, foreignCategory, start, "50");
        expense(other, foreignCategory, start, "9999");
        expense(other, foreignCategory, start.minusDays(1), "9999");
        expense(owner, food, LocalDate.of(2024, 1, 30), "10");
        expense(owner, food, LocalDate.of(2024, 2, 29), "10");
        expense(owner, food, LocalDate.of(2024, 1, 29), "9999");
        expense(owner, food, end.plusDays(1), "9999");
        entityManager.flush();
        entityManager.clear();

        var result = service.getExpenseAnalysis(start, end);

        assertThat(result.getTotalSpent()).isEqualByComparingTo("200");
        assertThat(result.getPreviousPeriodTotal()).isEqualByComparingTo("20");
        assertThat(result.getPercentageChange()).isEqualByComparingTo("900");
        assertThat(result.getPreviousStartDate()).isEqualTo(LocalDate.of(2024, 1, 30));
        assertThat(result.getPreviousEndDate()).isEqualTo(LocalDate.of(2024, 2, 29));
        assertThat(result.getCategories()).extracting(ExpenseCategorySummaryDTO::categoryName)
            .containsExactly("Food", "Sem categoria", "Transport");
        assertThat(result.getCategories()).extracting(ExpenseCategorySummaryDTO::amount)
            .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
            .containsExactly(new BigDecimal("100"), new BigDecimal("60"), new BigDecimal("40"));
        assertThat(result.getCategories().stream().map(ExpenseCategorySummaryDTO::percentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo("100");
        assertThat(result.getInsight().categoryId()).isEqualTo(food.getId());
    }

    @Test
    void emptySummaryReturnsZerosDespiteAnotherUsersExpenses() {
        var date = LocalDate.of(2026, 1, 1);
        expense(other, null, date, "100");
        expense(other, null, date.minusDays(1), "100");
        entityManager.flush();
        entityManager.clear();

        var result = service.getExpenseAnalysis(date, date);

        assertThat(result.getTotalSpent()).isZero();
        assertThat(result.getPreviousPeriodTotal()).isZero();
        assertThat(result.getPercentageChange()).isZero();
        assertThat(result.getCategories()).isEmpty();
        assertThat(result.getInsight()).isNull();
    }

    private Category category(String name, User user) {
        var category = new Category();
        category.setName(name);
        category.setUser(user);
        category.setIcon(CategoryIconEnum.FOOD);
        return entityManager.persist(category);
    }

    private Expense expense(User user, Category category, LocalDate date, String amount) {
        var expense = new Expense();
        expense.setUser(user);
        expense.setCategory(category);
        expense.setDate(date);
        expense.setAmount(new BigDecimal(amount));
        expense.setDescription("Test expense");
        return entityManager.persist(expense);
    }
}
