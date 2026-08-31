package com.brandon.finance.expense.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.expense.dto.ExpenseAnalysisDTO;
import com.brandon.finance.expense.dto.ExpenseDTO;
import com.brandon.finance.expense.entity.Expense;
import com.brandon.finance.expense.mapper.ExpenseMapper;
import com.brandon.finance.expense.repository.ExpenseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public ExpenseDTO create(ExpenseDTO dto) {
        Expense expense = expenseMapper.toEntity(dto);
        Expense saved = expenseRepository.save(expense);
        return expenseMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public ExpenseDTO getById(Long id) {
        return expenseRepository.findById(id)
            .map(expenseMapper::toDTO)
            .orElse(null);
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
        return expenseRepository.findById(id)
            .map(expense -> {
                expense.setDescription(dto.getDescription());
                expense.setAmount(dto.getAmount());
                expense.setDate(dto.getDate());
                Expense updated = expenseRepository.save(expense);
                return expenseMapper.toDTO(updated);
            })
            .orElse(null);
    }

    @Transactional
    public void delete(Long id) {
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ExpenseAnalysisDTO getExpenseAnalysis(LocalDate startDate, LocalDate endDate) {
        Long userId = authenticatedUserService.getUserId();
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        List<Expense> previousExpenses = expenseRepository.findByUserIdAndDateBetween(
            userId,
            startDate.minusMonths(1),
            endDate.minusMonths(1)
        );

        BigDecimal totalSpent = sumAmounts(expenses);
        BigDecimal previousPeriodTotal = sumAmounts(previousExpenses);
        BigDecimal percentageChange = calculatePercentageChange(totalSpent, previousPeriodTotal);

        ExpenseAnalysisDTO analysisDTO = new ExpenseAnalysisDTO();
        analysisDTO.setTotalSpent(totalSpent);
        analysisDTO.setPreviousPeriodTotal(previousPeriodTotal);
        analysisDTO.setPercentageChange(percentageChange);
        return analysisDTO;
    }

    private BigDecimal sumAmounts(List<Expense> expenses) {
        return expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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
