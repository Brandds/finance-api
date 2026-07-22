package com.brandon.finance.user.mapper;


import org.springframework.stereotype.Component;

import com.brandon.finance.shared.Utils.CpfUtils;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.enums.Role;
import com.brandon.finance.user.request.CreateUserRequest;
import com.brandon.finance.user.response.UserEditResponse;
import com.brandon.finance.user.response.UserResponse;

@Component
public class UserMapper {
    public User toEntity(CreateUserRequest request, String encodedPassword) {
        return new User(
            request.name(),
            request.email(),
            encodedPassword,
            Role.USER,
            CpfUtils.normalize(request.cpf())
        );
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getCpf()
        );
    }

    public UserResponse toResponseEdit(User user, UserEditResponse request) {
        return new UserResponse(
            user.getId(),
            request.name(),
            request.email(),
            user.getRole(),
            user.getCpf()
        );
    }

    public User toEntityEdit(User user, UserEditResponse request) {
        user.setName(request.name());
        user.setEmail(request.email());
        return user;
    }
}
