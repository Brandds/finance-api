package com.brandon.finance.category.mapper;

import org.springframework.stereotype.Component;

import com.brandon.finance.category.dto.CategoryDTO;
import com.brandon.finance.category.entity.Category;
import com.brandon.finance.user.entity.User;

@Component
public class CategoryMapper {

    public CategoryDTO toDTO(Category category) {
        if (category == null) {
            return null;
        }

        return new CategoryDTO(
            category.getId(),
            category.getName(),
            category.getUser() != null ? category.getUser().getId() : null
        );
    }

    public Category toEntity(CategoryDTO dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setUser(new User(dto.getUserId()));

        return category;
    }
}
