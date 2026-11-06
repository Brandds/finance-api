package com.brandon.finance.category.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.category.dto.CategoryDTO;
import com.brandon.finance.category.entity.Category;
import com.brandon.finance.category.mapper.CategoryMapper;
import com.brandon.finance.category.repository.CategoryRepository;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final AuthenticatedUserService authenticationService;

    @Transactional
    public CategoryDTO create(CategoryDTO dto) {
        Category category = categoryMapper.toEntity(dto);
        category.setUser(authenticationService.getUser());
        
        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public CategoryDTO getById(Long id) {
        return categoryMapper.toDTO(getOwnedCategory(id));
    }

    @Transactional(readOnly = true)
    public Page<CategoryDTO> getByUserList(Pageable pageable) {
        Page<Category> categories = categoryRepository.findByUserId(authenticationService.getUserId(), pageable);
        List<CategoryDTO> dtos = categories.getContent()
            .stream()
            .map(categoryMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, categories.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getByUserIdList() {
        return categoryRepository.findByUserId(authenticationService.getUserId())
            .stream()
            .map(categoryMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDTO getByUserIdAndName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Nome da categoria obrigatório");
        }
        Category category = categoryRepository.findByUserIdAndName(authenticationService.getUserId(), name);
        if (category == null) {
            throw new ResourceNotFoundException("Categoria não encontrada");
        }
        return categoryMapper.toDTO(category);
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        return categoryRepository.findByIdAndUserId(id, authenticationService.getUserId())
            .map(category -> {
                category.setName(dto.getName());
                Category updated = categoryRepository.save(category);
                return categoryMapper.toDTO(updated);
            })
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(getOwnedCategory(id));
    }

    private Category getOwnedCategory(Long id) {
        return categoryRepository.findByIdAndUserId(id, authenticationService.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }
}
