package com.brandon.finance.budget.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.budget.dto.BudgetDTO;
import com.brandon.finance.budget.entity.Budget;
import com.brandon.finance.budget.mapper.BudgetMapper;
import com.brandon.finance.budget.repository.BudgetRepository;
import com.brandon.finance.category.repository.CategoryRepository;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;
    private final AuthenticatedUserService authenticationService;
    private final CategoryRepository categoryRepository;

    @Transactional
    public BudgetDTO create(BudgetDTO dto) {
        Budget budget = budgetMapper.toEntity(dto);
        budget.setUser(authenticationService.getUser());
        budget.setCategory(categoryRepository.findByIdAndUserId(dto.getCategoryId(), budget.getUser().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada")));
        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public BudgetDTO getById(Long id) {
        return budgetMapper.toDTO(getOwnedBudget(id));
    }

    @Transactional(readOnly = true)
    public Page<BudgetDTO> getAllPage(Pageable pageable) {
        Page<Budget> budgets = budgetRepository.findByUserId(authenticationService.getUserId(), pageable);
        List<BudgetDTO> dtos = budgets.getContent()
            .stream()
            .map(budgetMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, budgets.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> getAll() {
        return budgetRepository.findByUserId(authenticationService.getUserId())
            .stream()
            .map(budgetMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> getByCategoryId(Long categoryId) {
        return budgetRepository.findByUserIdAndCategoryId(authenticationService.getUserId(), categoryId)
            .stream()
            .map(budgetMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> getByMonth(Integer month) {
        return budgetRepository.findByUserIdAndMonth(authenticationService.getUserId(), month)
            .stream()
            .map(budgetMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> getByYear(Integer year) {
        return budgetRepository.findByUserIdAndYear(authenticationService.getUserId(), year)
            .stream()
            .map(budgetMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> getByMonthAndYear(Integer month, Integer year) {
        return budgetRepository.findByUserIdAndMonthAndYear(authenticationService.getUserId(), month, year)
            .stream()
            .map(budgetMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BudgetDTO getByCategoryIdAndMonthAndYear(Long categoryId, Integer month, Integer year) {
        return budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(authenticationService.getUserId(), categoryId, month, year)
            .map(budgetMapper::toDTO)
            .orElse(null);
    }

    @Transactional
    public BudgetDTO update(Long id, BudgetDTO dto) {
        return budgetRepository.findByIdAndUserId(id, authenticationService.getUserId())
            .map(budget -> {
                budget.setLimitAmount(dto.getLimitAmount());
                budget.setMonth(dto.getMonth());
                budget.setYear(dto.getYear());
                Budget updated = budgetRepository.save(budget);
                return budgetMapper.toDTO(updated);
            })
            .orElseThrow(() -> new ResourceNotFoundException("Orçamento não encontrado"));
    }

    @Transactional
    public void delete(Long id) {
        budgetRepository.delete(getOwnedBudget(id));
    }

    private Budget getOwnedBudget(Long id) {
        return budgetRepository.findByIdAndUserId(id, authenticationService.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Orçamento não encontrado"));
    }
}
