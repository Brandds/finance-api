package com.brandon.finance.expense.specification;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.brandon.finance.expense.entity.Expense;

import jakarta.persistence.criteria.Predicate;

public final class ExpenseSpecification {
    private ExpenseSpecification() {}

    public static Specification<Expense> filter(Long userId, List<Long> categoryIds,
            LocalDate startDate, LocalDate endDate, BigDecimal minAmount, BigDecimal maxAmount, String description) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
            }
            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }
            if (description != null && !description.strip().isEmpty()) {
                String term = Normalizer.normalize(description.strip().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                    .replaceAll("[\\u0300-\\u036f]", "")
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_");
                // Hibernate sql() permits PostgreSQL's NFD keyword without an extension.
                var normalized = cb.function("sql", String.class,
                    cb.literal("regexp_replace(normalize(lower(?), NFD), '[\u0300-\u036f]', '', 'g')"),
                    root.get("description"));
                predicates.add(cb.like(normalized, "%" + term + "%", '!'));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
