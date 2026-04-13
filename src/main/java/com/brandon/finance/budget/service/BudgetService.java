package com.brandon.finance.budget.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.budget.dto.BudgetDTO;
import com.brandon.finance.budget.entity.Budget;
import com.brandon.finance.budget.mapper.BudgetMapper;
import com.brandon.finance.budget.repository.BudgetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;

    @Transactional
    public BudgetDTO create(BudgetDTO dto) {
        Budget budget = budgetMapper.toEntity(dto);
        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public BudgetDTO getById(Long id) {
        return budgetRepository.findById(id)
            .map(budgetMapper::toDTO)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<BudgetDTO> getByUserId(Long userId, Pageable pageable) {
        Page<Budget> budgets = budgetRepository.findByUserId(userId, pageable);
        List<BudgetDTO> dtos = budgets.getContent()
            .stream()
            .map(budgetMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, budgets.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> getByUserId(Long userId) {
        return budgetRepository.findByUserId(userId)
            .stream()
            .map(budgetMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> getByUserIdAndCategory(Long userId, Long categoryId) {
        return budgetRepository.findByUserIdAndCategoryId(userId, categoryId)
            .stream()
            .map(budgetMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> getByUserIdAndMonth(Long userId, Integer month) {
        return budgetRepository.findByUserIdAndMonth(userId, month)
            .stream()
            .map(budgetMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> getByUserIdAndYear(Long userId, Integer year) {
        return budgetRepository.findByUserIdAndYear(userId, year)
            .stream()
            .map(budgetMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> getByUserIdAndMonthAndYear(Long userId, Integer month, Integer year) {
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year)
            .stream()
            .map(budgetMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BudgetDTO getByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year) {
        return budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, month, year)
            .map(budgetMapper::toDTO)
            .orElse(null);
    }

    @Transactional
    public BudgetDTO update(Long id, BudgetDTO dto) {
        return budgetRepository.findById(id)
            .map(budget -> {
                budget.setLimitAmount(dto.getLimitAmount());
                budget.setMonth(dto.getMonth());
                budget.setYear(dto.getYear());
                Budget updated = budgetRepository.save(budget);
                return budgetMapper.toDTO(updated);
            })
            .orElse(null);
    }

    @Transactional
    public void delete(Long id) {
        budgetRepository.deleteById(id);
    }
}
