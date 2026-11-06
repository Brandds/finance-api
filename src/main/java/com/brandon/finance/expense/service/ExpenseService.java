package com.brandon.finance.expense.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.account.repository.AccountRepository;
import com.brandon.finance.category.repository.CategoryRepository;
import com.brandon.finance.expense.dto.ExpenseAnalysisDTO;
import com.brandon.finance.expense.dto.ExpenseCategorySummaryDTO;
import com.brandon.finance.expense.dto.ExpenseDTO;
import com.brandon.finance.expense.dto.ExpenseFilterRequest;
import com.brandon.finance.expense.dto.ExpenseInsightDTO;
import com.brandon.finance.expense.entity.Expense;
import com.brandon.finance.expense.enums.PeriodType;
import com.brandon.finance.expense.mapper.ExpenseMapper;
import com.brandon.finance.expense.repository.CategoryExpenseTotal;
import com.brandon.finance.expense.repository.ExpenseRepository;
import com.brandon.finance.expense.specification.ExpenseSpecification;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final AuthenticatedUserService authenticatedUserService;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public Page<ExpenseDTO> filter(ExpenseFilterRequest filter, Pageable pageable) {
        LocalDate startDate = filter.getStartDate();
        LocalDate endDate = filter.getEndDate();
        PeriodType period = filter.getPeriodType();
        if (period != null) {
            LocalDate today = LocalDate.now();
            switch (period) {
                case TODAY -> { startDate = today; endDate = today; }
                case LAST_7_DAYS -> { startDate = today.minusDays(6); endDate = today; }
                case CURRENT_MONTH -> {
                    startDate = today.withDayOfMonth(1);
                    endDate = today.withDayOfMonth(today.lengthOfMonth());
                }
                case CUSTOM -> {
                    if (startDate == null || endDate == null) {
                        throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                            "Período personalizado exige startDate e endDate");
                    }
                }
            }
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "startDate deve ser anterior ou igual a endDate");
        }
        if (filter.getMinAmount() != null && filter.getMaxAmount() != null
                && filter.getMinAmount().compareTo(filter.getMaxAmount()) > 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "minAmount deve ser menor ou igual a maxAmount");
        }

        return expenseRepository.findAll(ExpenseSpecification.filter(
                authenticatedUserService.getUserId(), filter.getCategoryIds(), startDate, endDate,
                filter.getMinAmount(), filter.getMaxAmount()), pageable)
            .map(expenseMapper::toDTO);
    }

    @Transactional
    public ExpenseDTO create(ExpenseDTO dto) {
        Expense expense = expenseMapper.toEntity(dto);
        expense.setUser(authenticatedUserService.getUser());
        Long userId = expense.getUser().getId();
        expense.setCategory(dto.getCategoryId() == null ? null
            : categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada")));
        expense.setAccount(dto.getAccountId() == null ? null
            : accountRepository.findByIdAndUserId(dto.getAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada")));
        Expense saved = expenseRepository.save(expense);
        return expenseMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public ExpenseDTO getById(Long id) {
        return expenseMapper.toDTO(getOwnedExpense(id));
    }

    @Transactional(readOnly = true)
    public Page<ExpenseDTO> getAllPage(Pageable pageable) {
        Page<Expense> expenses = expenseRepository.findByUserId(authenticatedUserService.getUserId(), pageable);
        List<ExpenseDTO> dtos = expenses.getContent()
            .stream()
            .map(expenseMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, expenses.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getByCategoryId(Long categoryId) {
        return expenseRepository.findByUserIdAndCategoryId(authenticatedUserService.getUserId(), categoryId)
            .stream()
            .map(expenseMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ExpenseDTO> getByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(authenticatedUserService.getUserId(), startDate, endDate, pageable);
        List<ExpenseDTO> dtos = expenses.getContent()
            .stream()
            .map(expenseMapper::toDTOByDateRanger)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, expenses.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getByAccountId(Long accountId) {
        return expenseRepository.findByUserIdAndAccountId(authenticatedUserService.getUserId(), accountId)
            .stream()
            .map(expenseMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseDTO update(Long id, ExpenseDTO dto) {
        return expenseRepository.findByIdAndUserId(id, authenticatedUserService.getUserId())
            .map(expense -> {
                expense.setDescription(dto.getDescription());
                expense.setAmount(dto.getAmount());
                expense.setDate(dto.getDate());
                Expense updated = expenseRepository.save(expense);
                return expenseMapper.toDTO(updated);
            })
            .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada"));
    }

    @Transactional
    public void delete(Long id) {
        expenseRepository.delete(getOwnedExpense(id));
    }

    private Expense getOwnedExpense(Long id) {
        return expenseRepository.findByIdAndUserId(id, authenticatedUserService.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada"));
    }

    @Transactional(readOnly = true)
    public ExpenseAnalysisDTO getExpenseAnalysis(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                "Informe startDate e endDate com startDate anterior ou igual a endDate");
        }
        LocalDate previousStartDate;
        LocalDate previousEndDate;
        try {
            long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            previousStartDate = startDate.minusDays(days);
            previousEndDate = startDate.minusDays(1);
        } catch (DateTimeException ex) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                "Período fora do intervalo de datas suportado");
        }
        Long userId = authenticatedUserService.getUserId();
        List<CategoryExpenseTotal> totals = expenseRepository.summarizeByCategory(userId, startDate, endDate);
        BigDecimal totalSpent = totals.stream().map(CategoryExpenseTotal::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal previousPeriodTotal = expenseRepository.sumByUserAndPeriod(
            userId, previousStartDate, previousEndDate);
        BigDecimal percentageChange = calculatePercentageChange(totalSpent, previousPeriodTotal);
        List<ExpenseCategorySummaryDTO> categories = totals.stream()
            .map(category -> new ExpenseCategorySummaryDTO(category.getCategoryId(),
                category.getCategoryName() == null ? "Sem categoria" : category.getCategoryName(),
                category.getAmount(), totalSpent.signum() == 0 ? BigDecimal.ZERO
                    : category.getAmount().multiply(BigDecimal.valueOf(100))
                        .divide(totalSpent, 2, RoundingMode.HALF_UP)))
            .sorted(Comparator.comparing(ExpenseCategorySummaryDTO::amount).reversed()
                .thenComparing(ExpenseCategorySummaryDTO::categoryName)
                .thenComparing(ExpenseCategorySummaryDTO::categoryId, Comparator.nullsLast(Long::compareTo)))
            .toList();

        ExpenseAnalysisDTO analysisDTO = new ExpenseAnalysisDTO();
        analysisDTO.setTotalSpent(totalSpent);
        analysisDTO.setPreviousPeriodTotal(previousPeriodTotal);
        analysisDTO.setPercentageChange(percentageChange);
        analysisDTO.setStartDate(startDate);
        analysisDTO.setEndDate(endDate);
        analysisDTO.setPreviousStartDate(previousStartDate);
        analysisDTO.setPreviousEndDate(previousEndDate);
        analysisDTO.setCategories(categories);
        if (totalSpent.signum() > 0 && !categories.isEmpty()) {
            ExpenseCategorySummaryDTO largest = categories.getFirst();
            analysisDTO.setInsight(new ExpenseInsightDTO("LARGEST_CATEGORY", largest.categoryId(),
                largest.categoryName(), largest.amount(), largest.percentage()));
        }
        return analysisDTO;
    }

    private BigDecimal calculatePercentageChange(BigDecimal currentTotal, BigDecimal previousTotal) {
        if (previousTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return currentTotal.subtract(previousTotal)
            .multiply(BigDecimal.valueOf(100))
            .divide(previousTotal, 2, RoundingMode.HALF_UP);
    }
}
