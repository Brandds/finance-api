package com.brandon.finance.category.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.category.dto.CategoryDTO;
import com.brandon.finance.category.entity.Category;
import com.brandon.finance.category.mapper.CategoryMapper;
import com.brandon.finance.category.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryDTO create(CategoryDTO dto) {
        Category category = categoryMapper.toEntity(dto);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public CategoryDTO getById(Long id) {
        return categoryRepository.findById(id)
            .map(categoryMapper::toDTO)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<CategoryDTO> getByUserId(Long userId, Pageable pageable) {
        Page<Category> categories = categoryRepository.findByUserId(userId, pageable);
        List<CategoryDTO> dtos = categories.getContent()
            .stream()
            .map(categoryMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, categories.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getByUserIdList(Long userId) {
        return categoryRepository.findByUserId(userId)
            .stream()
            .map(categoryMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDTO getByUserIdAndName(Long userId, String name) {
        Category category = categoryRepository.findByUserIdAndName(userId, name);
        return categoryMapper.toDTO(category);
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        return categoryRepository.findById(id)
            .map(category -> {
                category.setName(dto.getName());
                Category updated = categoryRepository.save(category);
                return categoryMapper.toDTO(updated);
            })
            .orElse(null);
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
