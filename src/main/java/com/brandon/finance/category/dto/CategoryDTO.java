package com.brandon.finance.category.dto;

import com.brandon.finance.category.enums.CategoryIconEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Long id;
    private String name;
    private CategoryIconEnum icon;
}
