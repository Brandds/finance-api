package com.brandon.finance.shared.constants;

public final class ApiPaths {
    private ApiPaths() {}

    public static final String API_V1 = "/api/v1";

    public static final String AUTH = API_V1 + "/auth";
    public static final String USERS = API_V1 + "/users";
    public static final String EXPENSES = API_V1 + "/expenses";
    public static final String CATEGORIES = API_V1 + "/categories";
    public static final String TAGS = API_V1 + "/tags";
    public static final String AUDIT_LOGS = API_V1 + "/audit-logs";
    public static final String BUDGETS = API_V1 + "/budgets";
    public static final String ACCOUNTS = API_V1 + "/accounts";
    public static final String FINANCIAL_TRANSACTIONS = API_V1 + "/financial-transactions";
    public static final String EXPENSE_TAGS = API_V1 + "/expense-tags";
    public static final String AUTH_GOOGLE = AUTH + "/google";
}
