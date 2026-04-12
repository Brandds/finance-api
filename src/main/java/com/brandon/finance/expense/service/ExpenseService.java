package com.brandon.finance.expense.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public ExpenseDTO create(ExpenseDTO dto) {
        Expense expense = expenseMapper.toEntity(dto);
        Expense saved = expenseRepository.save(expense);
        return expenseMapper.toDTO(saved);
    }

    public ExpenseDTO getById(Long id) {
        return expenseRepository.findById(id)
            .map(expenseMapper::toDTO)
            .orElse(null);
    }

    public Page<ExpenseDTO> getByUserId(Long userId, Pageable pageable) {
        Page<Expense> expenses = expenseRepository.findByUserId(userId, pageable);
        List<ExpenseDTO> dtos = expenses.getContent()
            .stream()
            .map(expenseMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, expenses.getTotalElements());
    }

    public List<ExpenseDTO> getByUserIdAndCategory(Long userId, Long categoryId) {
        return expenseRepository.findByUserIdAndCategoryId(userId, categoryId)
            .stream()
            .map(expenseMapper::toDTO)
            .collect(Collectors.toList());
    }

    public Page<ExpenseDTO> getByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate, pageable);
        List<ExpenseDTO> dtos = expenses.getContent()
            .stream()
            .map(expenseMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, expenses.getTotalElements());
    }

    public List<ExpenseDTO> getByUserIdAndAccount(Long userId, Long accountId) {
        return expenseRepository.findByUserIdAndAccountId(userId, accountId)
            .stream()
            .map(expenseMapper::toDTO)
            .collect(Collectors.toList());
    }

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

    public void delete(Long id) {
        expenseRepository.deleteById(id);
    }
}
