package com.brandon.finance.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.category.entity.Category;
import com.brandon.finance.category.enums.CategoryIconEnum;
import com.brandon.finance.category.mapper.CategoryMapper;
import com.brandon.finance.category.repository.CategoryRepository;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock CategoryRepository repository;
    @Mock AuthenticatedUserService authenticatedUserService;
    @Spy CategoryMapper mapper = new CategoryMapper();
    @InjectMocks CategoryService service;

    @Test
    void looksUpNameUsingOnlyAuthenticatedUsersId() {
        var category = new Category(7L);
        category.setName("Alimentação");
        category.setIcon(CategoryIconEnum.FOOD);
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        when(repository.findByUserIdAndName(42L, "Alimentação")).thenReturn(category);

        var response = service.getByUserIdAndName("Alimentação");

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getName()).isEqualTo("Alimentação");
        assertThat(response.getIcon()).isEqualTo(CategoryIconEnum.FOOD);
        verify(repository).findByUserIdAndName(42L, "Alimentação");
    }

    @Test
    void reportsCategoryNotFoundForAuthenticatedUser() {
        when(authenticatedUserService.getUserId()).thenReturn(42L);

        assertThatThrownBy(() -> service.getByUserIdAndName("Outra categoria"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsBlankNameBeforeQueryingRepository() {
        assertThatThrownBy(() -> service.getByUserIdAndName(" "))
            .isInstanceOfSatisfying(BusinessException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
        verifyNoInteractions(repository);
    }
}
