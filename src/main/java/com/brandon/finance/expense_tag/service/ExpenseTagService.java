package com.brandon.finance.expense_tag.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.expense.entity.Expense;
import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.expense.repository.ExpenseRepository;
import com.brandon.finance.expense_tag.dto.ExpenseTagDTO;
import com.brandon.finance.expense_tag.entity.ExpenseTag;
import com.brandon.finance.expense_tag.entity.ExpenseTagId;
import com.brandon.finance.expense_tag.mapper.ExpenseTagMapper;
import com.brandon.finance.expense_tag.repository.ExpenseTagRepository;
import com.brandon.finance.tag.entity.Tag;
import com.brandon.finance.tag.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseTagService {

    private final ExpenseTagRepository expenseTagRepository;
    private final ExpenseTagMapper expenseTagMapper;
    private final ExpenseRepository expenseRepository;
    private final TagRepository tagRepository;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public ExpenseTagDTO create(ExpenseTagDTO dto) {
        Long userId = authenticatedUserService.getUserId();
        Expense expense = getOwnedExpense(dto.getExpenseId(), userId);
        Tag tag = getOwnedTag(dto.getTagId(), userId);

        ExpenseTag expenseTag = new ExpenseTag();
        expenseTag.setId(new ExpenseTagId(dto.getExpenseId(), dto.getTagId()));
        expenseTag.setExpense(expense);
        expenseTag.setTag(tag);

        ExpenseTag saved = expenseTagRepository.save(expenseTag);
        return expenseTagMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseTagDTO> getByExpenseId(Long expenseId) {
        Long userId = authenticatedUserService.getUserId();
        getOwnedExpense(expenseId, userId);
        return expenseTagRepository.findByIdExpenseIdAndExpenseUserIdAndTagUserId(expenseId, userId, userId)
            .stream()
            .map(expenseTagMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ExpenseTagDTO> getByExpenseIdPage(Long expenseId, Pageable pageable) {
        Long userId = authenticatedUserService.getUserId();
        getOwnedExpense(expenseId, userId);
        Page<ExpenseTag> expenseTags = expenseTagRepository.findByIdExpenseIdAndExpenseUserIdAndTagUserId(expenseId, userId, userId, pageable);
        List<ExpenseTagDTO> dtos = expenseTags.getContent()
            .stream()
            .map(expenseTagMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, expenseTags.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<ExpenseTagDTO> getByTagId(Long tagId) {
        Long userId = authenticatedUserService.getUserId();
        getOwnedTag(tagId, userId);
        return expenseTagRepository.findByIdTagIdAndExpenseUserIdAndTagUserId(tagId, userId, userId)
            .stream()
            .map(expenseTagMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ExpenseTagDTO> getByTagIdPage(Long tagId, Pageable pageable) {
        Long userId = authenticatedUserService.getUserId();
        getOwnedTag(tagId, userId);
        Page<ExpenseTag> expenseTags = expenseTagRepository.findByIdTagIdAndExpenseUserIdAndTagUserId(tagId, userId, userId, pageable);
        List<ExpenseTagDTO> dtos = expenseTags.getContent()
            .stream()
            .map(expenseTagMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, expenseTags.getTotalElements());
    }

    @Transactional
    public void delete(Long expenseId, Long tagId) {
        Long userId = authenticatedUserService.getUserId();
        ExpenseTag link = expenseTagRepository.findByIdAndExpenseUserIdAndTagUserId(
            new ExpenseTagId(expenseId, tagId), userId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Vínculo de despesa e tag não encontrado"));
        expenseTagRepository.delete(link);
    }

    private Expense getOwnedExpense(Long id, Long userId) {
        return expenseRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada"));
    }

    private Tag getOwnedTag(Long id, Long userId) {
        return tagRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Tag não encontrada"));
    }
}
