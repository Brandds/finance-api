package com.brandon.finance.isolation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.brandon.finance.account.controller.AccountController;
import com.brandon.finance.account.entity.Account;
import com.brandon.finance.account.mapper.AccountMapper;
import com.brandon.finance.account.repository.AccountRepository;
import com.brandon.finance.account.service.AccountService;
import com.brandon.finance.auth.security.JwtFilter;
import com.brandon.finance.auth.service.JwtService;
import com.brandon.finance.authentication.service.impl.AuthenticatedUserServiceImpl;
import com.brandon.finance.budget.controller.BudgetController;
import com.brandon.finance.budget.entity.Budget;
import com.brandon.finance.budget.mapper.BudgetMapper;
import com.brandon.finance.budget.repository.BudgetRepository;
import com.brandon.finance.budget.service.BudgetService;
import com.brandon.finance.category.controller.CategoryController;
import com.brandon.finance.category.entity.Category;
import com.brandon.finance.category.mapper.CategoryMapper;
import com.brandon.finance.category.repository.CategoryRepository;
import com.brandon.finance.category.service.CategoryService;
import com.brandon.finance.config.SecurityConfig;
import com.brandon.finance.expense.controller.ExpenseController;
import com.brandon.finance.expense.entity.Expense;
import com.brandon.finance.expense.mapper.ExpenseMapper;
import com.brandon.finance.expense.repository.ExpenseRepository;
import com.brandon.finance.expense.service.ExpenseService;
import com.brandon.finance.expense_tag.controller.ExpenseTagController;
import com.brandon.finance.expense_tag.entity.ExpenseTag;
import com.brandon.finance.expense_tag.entity.ExpenseTagId;
import com.brandon.finance.expense_tag.mapper.ExpenseTagMapper;
import com.brandon.finance.expense_tag.repository.ExpenseTagRepository;
import com.brandon.finance.expense_tag.service.ExpenseTagService;
import com.brandon.finance.financial_transaction.controller.FinancialTransactionController;
import com.brandon.finance.financial_transaction.entity.FinancialTransaction;
import com.brandon.finance.financial_transaction.mapper.FinancialTransactionMapper;
import com.brandon.finance.financial_transaction.repository.FinancialTransactionRepository;
import com.brandon.finance.financial_transaction.service.FinancialTransactionService;
import com.brandon.finance.log.controller.AuditLogController;
import com.brandon.finance.log.mapper.AuditLogMapper;
import com.brandon.finance.log.repository.AuditLogRepository;
import com.brandon.finance.log.service.AuditLogService;
import com.brandon.finance.shared.base.excepetion.CustomAccessDeniedHandler;
import com.brandon.finance.shared.base.excepetion.CustomAuthenticationEntryPoint;
import com.brandon.finance.shared.base.excepetion.GlobalExceptionHandler;
import com.brandon.finance.shared.base.response.PageSerializer;
import com.brandon.finance.tag.controller.TagController;
import com.brandon.finance.tag.entity.Tag;
import com.brandon.finance.tag.mapper.TagMapper;
import com.brandon.finance.tag.repository.TagRepository;
import com.brandon.finance.tag.service.TagService;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.repository.UserRepository;

@WebMvcTest
@ContextConfiguration(classes = {AccountController.class, AccountService.class, AccountMapper.class,
    CategoryController.class, CategoryService.class, CategoryMapper.class,
    ExpenseController.class, ExpenseService.class, ExpenseMapper.class,
    BudgetController.class, BudgetService.class, BudgetMapper.class,
    FinancialTransactionController.class, FinancialTransactionService.class, FinancialTransactionMapper.class,
    TagController.class, TagService.class, TagMapper.class,
    ExpenseTagController.class, ExpenseTagService.class, ExpenseTagMapper.class,
    AuditLogController.class, AuditLogService.class, AuditLogMapper.class,
    AuthenticatedUserServiceImpl.class, SecurityConfig.class, JwtFilter.class, PageSerializer.class,
    GlobalExceptionHandler.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
class FinancialIsolationWebTest {
    private static final String AUTH = "Bearer valid-token";
    private static final String BODY = """
        {"id":999,"userId":43,"name":"Updated","description":"Updated","amount":10,
         "limitAmount":100,"month":1,"year":2026,"date":"2026-01-01"}
        """;
    @Autowired MockMvc mvc;
    @MockitoBean AccountRepository accounts;
    @MockitoBean CategoryRepository categories;
    @MockitoBean ExpenseRepository expenses;
    @MockitoBean BudgetRepository budgets;
    @MockitoBean FinancialTransactionRepository transactions;
    @MockitoBean TagRepository tags;
    @MockitoBean ExpenseTagRepository links;
    @MockitoBean AuditLogRepository auditLogs;
    @MockitoBean UserRepository users;
    @MockitoBean JwtService jwt;
    private final User owner = new User(42L);

    @BeforeEach
    void authenticate() {
        when(jwt.extractUserId("valid-token")).thenReturn(42L);
        when(jwt.extractEmail("valid-token")).thenReturn("owner@example.test");
        when(jwt.extractRole("valid-token")).thenReturn("USER");
        when(users.findById(42L)).thenReturn(Optional.of(owner));
    }

    @ParameterizedTest
    @ValueSource(strings = {"accounts", "categories", "expenses", "budgets", "financial-transactions", "tags"})
    void foreignAndMissingIdsReturnSame404ForReadUpdateAndDelete(String resource) throws Exception {
        for (String id : List.of("7", "999")) {
            var path = "/api/v1/" + resource + "/" + id;
            mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, AUTH).param("userId", "43"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
            mvc.perform(put(path).header(HttpHeaders.AUTHORIZATION, AUTH).param("userId", "43")
                    .contentType(MediaType.APPLICATION_JSON).content(body(resource)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
            mvc.perform(delete(path).header(HttpHeaders.AUTHORIZATION, AUTH).param("userId", "43"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }
        verifyNoWrites();
    }

    @ParameterizedTest
    @ValueSource(strings = {"accounts", "categories", "expenses", "budgets", "financial-transactions", "tags"})
    void ownerCanReadUpdateAndDeleteWithoutTransferringOwnership(String resource) throws Exception {
        allowOwnedResources();
        var path = "/api/v1/" + resource + "/7";
        mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, AUTH))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(7));
        mvc.perform(put(path).header(HttpHeaders.AUTHORIZATION, AUTH).param("userId", "43")
                .contentType(MediaType.APPLICATION_JSON).content(body(resource)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(7));
        mvc.perform(delete(path).header(HttpHeaders.AUTHORIZATION, AUTH))
            .andExpect(status().isNoContent());
        verifySavedAndDeletedOwner(resource);
    }

    @ParameterizedTest
    @ValueSource(strings = {"accounts", "categories", "expenses", "budgets", "financial-transactions", "tags"})
    void creationIgnoresUserIdAndRecordIdFromClient(String resource) throws Exception {
        allowOwnedResources();
        // Orçamento exige categoria própria; as demais associações são opcionais.
        String body = resource.equals("budgets") ? body(resource).replace("\"id\":999", "\"categoryId\":7,\"id\":999") : body(resource);
        mvc.perform(post("/api/v1/" + resource).header(HttpHeaders.AUTHORIZATION, AUTH).param("userId", "43")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());
        switch (resource) {
            case "accounts" -> verify(accounts).save(argThat(e -> e.getUser().getId().equals(42L) && e.getId() == null));
            case "categories" -> verify(categories).save(argThat(e -> e.getUser().getId().equals(42L) && e.getId() == null));
            case "expenses" -> verify(expenses).save(argThat(e -> e.getUser().getId().equals(42L) && e.getId() == null));
            case "budgets" -> verify(budgets).save(argThat(e -> e.getUser().getId().equals(42L) && e.getId() == null));
            case "financial-transactions" -> verify(transactions).save(argThat(e -> e.getUser().getId().equals(42L) && e.getId() == null));
            case "tags" -> verify(tags).save(argThat(e -> e.getUser().getId().equals(42L) && e.getId() == null));
            default -> throw new AssertionError(resource);
        }
        verify(users, never()).findById(43L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"expenses", "budgets", "financial-transactions"})
    void creationRejectsForeignCategoryWithoutSaving(String resource) throws Exception {
        mvc.perform(post("/api/v1/" + resource).header(HttpHeaders.AUTHORIZATION, AUTH)
                .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":43,\"categoryId\":7}"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        verify(categories).findByIdAndUserId(7L, 42L);
        verifyNoWrites();
    }

    @ParameterizedTest
    @ValueSource(strings = {"expenses", "financial-transactions"})
    void creationRejectsForeignAccountWithoutSaving(String resource) throws Exception {
        mvc.perform(post("/api/v1/" + resource).header(HttpHeaders.AUTHORIZATION, AUTH)
                .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":43,\"accountId\":7}"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        verify(accounts).findByIdAndUserId(7L, 42L);
        verifyNoWrites();
    }

    @ParameterizedTest
    @ValueSource(strings = {"expenses", "financial-transactions"})
    void creationAcceptsOwnedCategoryAndAccount(String resource) throws Exception {
        allowOwnedResources();
        mvc.perform(post("/api/v1/" + resource).header(HttpHeaders.AUTHORIZATION, AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(resource).replace("\"id\":999", "\"categoryId\":7,\"accountId\":7,\"id\":999")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.userId").value(42))
            .andExpect(jsonPath("$.data.categoryId").value(7))
            .andExpect(jsonPath("$.data.accountId").value(7));
        verify(categories).findByIdAndUserId(7L, 42L);
        verify(accounts).findByIdAndUserId(7L, 42L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"accounts", "categories", "expenses", "budgets", "financial-transactions", "tags"})
    void paginatedListsIgnoreClientUserId(String resource) throws Exception {
        when(accounts.findByUserId(eq(42L), any(Pageable.class))).thenReturn(Page.empty());
        when(categories.findByUserId(eq(42L), any(Pageable.class))).thenReturn(Page.empty());
        when(expenses.findByUserId(eq(42L), any(Pageable.class))).thenReturn(Page.empty());
        when(budgets.findByUserId(eq(42L), any(Pageable.class))).thenReturn(Page.empty());
        when(transactions.findByUserId(eq(42L), any(Pageable.class))).thenReturn(Page.empty());
        when(tags.findByUserId(eq(42L), any(Pageable.class))).thenReturn(Page.empty());
        mvc.perform(get("/api/v1/" + resource).header(HttpHeaders.AUTHORIZATION, AUTH).param("userId", "43"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").isEmpty());
        switch (resource) {
            case "accounts" -> verify(accounts).findByUserId(eq(42L), any(Pageable.class));
            case "categories" -> verify(categories).findByUserId(eq(42L), any(Pageable.class));
            case "expenses" -> verify(expenses).findByUserId(eq(42L), any(Pageable.class));
            case "budgets" -> verify(budgets).findByUserId(eq(42L), any(Pageable.class));
            case "financial-transactions" -> verify(transactions).findByUserId(eq(42L), any(Pageable.class));
            case "tags" -> verify(tags).findByUserId(eq(42L), any(Pageable.class));
            default -> throw new AssertionError(resource);
        }
    }

    @Test
    void expenseTagListsRejectForeignParentIds() throws Exception {
        for (String suffix : List.of("/expense/7", "/expense/7/page", "/tag/7", "/tag/7/page")) {
            mvc.perform(get("/api/v1/expense-tags" + suffix).header(HttpHeaders.AUTHORIZATION, AUTH))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }
        verifyNoInteractions(links);
    }

    @Test
    void cannotLinkForeignExpenseOrTag() throws Exception {
        mvc.perform(post("/api/v1/expense-tags").header(HttpHeaders.AUTHORIZATION, AUTH)
                .contentType(MediaType.APPLICATION_JSON).content("{\"expenseId\":7,\"tagId\":8}"))
            .andExpect(status().isNotFound());
        var expense = new Expense();
        expense.setUser(owner);
        when(expenses.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(expense));
        mvc.perform(post("/api/v1/expense-tags").header(HttpHeaders.AUTHORIZATION, AUTH)
                .contentType(MediaType.APPLICATION_JSON).content("{\"expenseId\":7,\"tagId\":8}"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        verifyNoInteractions(links);
    }

    @Test
    void cannotDeleteForeignOrMixedOwnerLink() throws Exception {
        mvc.perform(delete("/api/v1/expense-tags/expense/7/tag/8").header(HttpHeaders.AUTHORIZATION, AUTH))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        verify(links).findByIdAndExpenseUserIdAndTagUserId(new ExpenseTagId(7L, 8L), 42L, 42L);
        verify(links, never()).delete(any(ExpenseTag.class));
    }

    @Test
    void ownerCanCreateListAndDeleteLinksAndBothOwnersScopeEveryQuery() throws Exception {
        allowOwnedResources();
        var id = new ExpenseTagId(7L, 7L);
        var link = new ExpenseTag();
        link.setId(id);
        when(links.save(any())).thenAnswer(call -> call.getArgument(0));
        when(links.findByIdAndExpenseUserIdAndTagUserId(id, 42L, 42L)).thenReturn(Optional.of(link));
        when(links.findByIdExpenseIdAndExpenseUserIdAndTagUserId(eq(7L), eq(42L), eq(42L), any()))
            .thenReturn(Page.empty());
        when(links.findByIdTagIdAndExpenseUserIdAndTagUserId(eq(7L), eq(42L), eq(42L), any()))
            .thenReturn(Page.empty());

        mvc.perform(post("/api/v1/expense-tags").header(HttpHeaders.AUTHORIZATION, AUTH)
                .contentType(MediaType.APPLICATION_JSON).content("{\"expenseId\":7,\"tagId\":7}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.data.expenseId").value(7));
        for (String suffix : List.of("/expense/7", "/expense/7/page", "/tag/7", "/tag/7/page")) {
            mvc.perform(get("/api/v1/expense-tags" + suffix).header(HttpHeaders.AUTHORIZATION, AUTH))
                .andExpect(status().isOk());
        }
        mvc.perform(delete("/api/v1/expense-tags/expense/7/tag/7").header(HttpHeaders.AUTHORIZATION, AUTH))
            .andExpect(status().isNoContent());
        verify(links).save(argThat(e -> e.getExpense().getUser().getId().equals(42L)
            && e.getTag().getUser().getId().equals(42L)));
        verify(links).findByIdExpenseIdAndExpenseUserIdAndTagUserId(7L, 42L, 42L);
        verify(links).findByIdTagIdAndExpenseUserIdAndTagUserId(7L, 42L, 42L);
        verify(links).findByIdExpenseIdAndExpenseUserIdAndTagUserId(eq(7L), eq(42L), eq(42L), any());
        verify(links).findByIdTagIdAndExpenseUserIdAndTagUserId(eq(7L), eq(42L), eq(42L), any());
        verify(links).delete(link);
    }

    @Test
    void tagListsAndSearchesIgnoreClientUserId() throws Exception {
        when(tags.findByUserId(eq(42L), any(Pageable.class))).thenReturn(Page.empty());
        when(tags.findByUserIdAndName(42L, "Test")).thenReturn(Optional.of(tag()));
        for (String suffix : List.of("?userId=43", "/list?userId=43", "/search/name?name=Test&userId=43", "/search?search=Test&userId=43")) {
            mvc.perform(get("/api/v1/tags" + suffix).header(HttpHeaders.AUTHORIZATION, AUTH)).andExpect(status().isOk());
        }
        verify(tags).findByUserId(eq(42L), any(Pageable.class));
        verify(tags).findByUserId(42L);
        verify(tags).findByUserIdAndName(42L, "Test");
        verify(tags).findByUserIdAndNameContainingIgnoreCase(42L, "Test");
    }

    @Test
    void allAuditQueriesRejectRegularUsersBeforeAccessingRepository() throws Exception {
        for (String suffix : List.of("/expense/7", "/table/expense", "/action/INSERT",
                "/period?startDate=2026-01-01T00:00:00&endDate=2026-01-31T23:59:59",
                "/table/expense/period?startDate=2026-01-01T00:00:00&endDate=2026-01-31T23:59:59")) {
            mvc.perform(get("/api/v1/audit-logs" + suffix).header(HttpHeaders.AUTHORIZATION, AUTH))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
        }
        verifyNoInteractions(auditLogs);
    }

    @Test
    void administratorCanConsultAuditHistory() throws Exception {
        when(jwt.extractRole("valid-token")).thenReturn("ADMIN");
        mvc.perform(get("/api/v1/audit-logs/expense/7").header(HttpHeaders.AUTHORIZATION, AUTH))
            .andExpect(status().isOk());
        verify(auditLogs).findByTableNameAndRecordId("expense", 7L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"accounts", "categories", "expenses", "budgets", "financial-transactions", "tags", "audit-logs/expense"})
    void requiresAuthentication(String resource) throws Exception {
        mvc.perform(get("/api/v1/" + resource + "/7")).andExpect(status().isUnauthorized());
        verifyNoInteractions(accounts, categories, expenses, budgets, transactions, tags, auditLogs);
    }

    private void allowOwnedResources() {
        var account = new Account(7L);
        account.setUser(owner);
        var category = new Category(7L);
        category.setUser(owner);
        category.setName("Own category");
        var expense = new Expense();
        ReflectionTestUtils.setField(expense, "id", 7L);
        expense.setUser(owner);
        var budget = new Budget();
        ReflectionTestUtils.setField(budget, "id", 7L);
        budget.setUser(owner);
        var transaction = new FinancialTransaction();
        ReflectionTestUtils.setField(transaction, "id", 7L);
        transaction.setUser(owner);
        when(accounts.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(account));
        when(categories.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(category));
        when(expenses.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(expense));
        when(budgets.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(budget));
        when(transactions.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(transaction));
        when(tags.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(tag()));
        when(accounts.save(any())).thenAnswer(call -> call.getArgument(0));
        when(categories.save(any())).thenAnswer(call -> call.getArgument(0));
        when(expenses.save(any())).thenAnswer(call -> call.getArgument(0));
        when(budgets.save(any())).thenAnswer(call -> call.getArgument(0));
        when(transactions.save(any())).thenAnswer(call -> call.getArgument(0));
        when(tags.save(any())).thenAnswer(call -> call.getArgument(0));
    }

    private String body(String resource) {
        return switch (resource) {
            case "accounts" -> BODY.replace("\"id\":999", "\"type\":\"CASH\",\"id\":999");
            case "financial-transactions" -> BODY.replace("\"id\":999", "\"type\":\"EXPENSE\",\"id\":999");
            case "categories" -> BODY.replace("\"id\":999", "\"icon\":\"FOOD\",\"id\":999");
            default -> BODY;
        };
    }

    private Tag tag() {
        var tag = new Tag();
        ReflectionTestUtils.setField(tag, "id", 7L);
        tag.setUser(owner);
        return tag;
    }

    private void verifySavedAndDeletedOwner(String resource) {
        switch (resource) {
            case "accounts" -> { verify(accounts).save(argThat(e -> e.getUser().getId().equals(42L))); verify(accounts).delete(any(Account.class)); }
            case "categories" -> { verify(categories).save(argThat(e -> e.getUser().getId().equals(42L))); verify(categories).delete(any(Category.class)); }
            case "expenses" -> { verify(expenses).save(argThat(e -> e.getUser().getId().equals(42L))); verify(expenses).delete(any(Expense.class)); }
            case "budgets" -> { verify(budgets).save(argThat(e -> e.getUser().getId().equals(42L))); verify(budgets).delete(any(Budget.class)); }
            case "financial-transactions" -> { verify(transactions).save(argThat(e -> e.getUser().getId().equals(42L))); verify(transactions).delete(any(FinancialTransaction.class)); }
            case "tags" -> { verify(tags).save(argThat(e -> e.getUser().getId().equals(42L))); verify(tags).delete(any(Tag.class)); }
            default -> throw new AssertionError(resource);
        }
    }

    private void verifyNoWrites() {
        verify(accounts, never()).save(any()); verify(accounts, never()).delete(any());
        verify(categories, never()).save(any()); verify(categories, never()).delete(any());
        verify(expenses, never()).save(any()); verify(expenses, never()).delete(any(Expense.class));
        verify(budgets, never()).save(any()); verify(budgets, never()).delete(any());
        verify(transactions, never()).save(any()); verify(transactions, never()).delete(any());
        verify(tags, never()).save(any()); verify(tags, never()).delete(any());
    }
}
