package com.brandon.finance.category.mapper;

import org.springframework.stereotype.Component;

import com.brandon.finance.category.dto.CategoryDTO;
import com.brandon.finance.category.entity.Category;

@Component
public class CategoryMapper {

    public CategoryDTO toDTO(Category category) {
        if (category == null) {
            return null;
        }

        return new CategoryDTO(
            category.getId(),
            category.getName()
        );
    }

    public Category toEntity(CategoryDTO dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setName(dto.getName());

        return category;
    }
}
