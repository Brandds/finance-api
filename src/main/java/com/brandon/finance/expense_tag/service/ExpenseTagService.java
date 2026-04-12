package com.brandon.finance.expense_tag.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.expense.entity.Expense;
import com.brandon.finance.expense.repository.ExpenseRepository;
import com.brandon.finance.expense_tag.dto.ExpenseTagDTO;
import com.brandon.finance.expense_tag.entity.ExpenseTag;
import com.brandon.finance.expense_tag.entity.ExpenseTagId;
import com.brandon.finance.expense_tag.mapper.ExpenseTagMapper;
import com.brandon.finance.expense_tag.repository.ExpenseTagRepository;
import com.brandon.finance.expense_tag.repository.TagRepository;
import com.brandon.finance.tag.entity.Tag;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseTagService {

    private final ExpenseTagRepository expenseTagRepository;
    private final ExpenseTagMapper expenseTagMapper;
    private final ExpenseRepository expenseRepository;
    private final TagRepository tagRepository;

    @Transactional
    public ExpenseTagDTO create(ExpenseTagDTO dto) {
        Expense expense = expenseRepository.findById(dto.getExpenseId()).orElse(null);
        Tag tag = tagRepository.findById(dto.getTagId()).orElse(null);

        if (expense == null || tag == null) {
            return null;
        }

        ExpenseTag expenseTag = new ExpenseTag();
        expenseTag.setId(new ExpenseTagId(dto.getExpenseId(), dto.getTagId()));
        expenseTag.setExpense(expense);
        expenseTag.setTag(tag);

        ExpenseTag saved = expenseTagRepository.save(expenseTag);
        return expenseTagMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseTagDTO> getByExpenseId(Long expenseId) {
        return expenseTagRepository.findByIdExpenseId(expenseId)
            .stream()
            .map(expenseTagMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ExpenseTagDTO> getByExpenseIdPage(Long expenseId, Pageable pageable) {
        Page<ExpenseTag> expenseTags = expenseTagRepository.findByIdExpenseId(expenseId, pageable);
        List<ExpenseTagDTO> dtos = expenseTags.getContent()
            .stream()
            .map(expenseTagMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, expenseTags.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<ExpenseTagDTO> getByTagId(Long tagId) {
        return expenseTagRepository.findByIdTagId(tagId)
            .stream()
            .map(expenseTagMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ExpenseTagDTO> getByTagIdPage(Long tagId, Pageable pageable) {
        Page<ExpenseTag> expenseTags = expenseTagRepository.findByIdTagId(tagId, pageable);
        List<ExpenseTagDTO> dtos = expenseTags.getContent()
            .stream()
            .map(expenseTagMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, expenseTags.getTotalElements());
    }

    @Transactional
    public void delete(Long expenseId, Long tagId) {
        expenseTagRepository.deleteById(new ExpenseTagId(expenseId, tagId));
    }
}
