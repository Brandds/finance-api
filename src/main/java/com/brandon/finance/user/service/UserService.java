package com.brandon.finance.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.brandon.finance.helper.SecurityUtils;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.shared.base.excepetion.UnauthorizedException;
import com.brandon.finance.user.mapper.UserMapper;
import com.brandon.finance.user.repository.UserRepository;
import com.brandon.finance.user.request.CreateUserRequest;
import com.brandon.finance.user.response.UserResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponse create(CreateUserRequest request) {
        
        String encodedPassword = passwordEncoder.encode(request.password());
        var user = userMapper.toEntity(request, encodedPassword);
        var savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    public UserResponse getUserById(Long id) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario não encontrado"));

        var userLoggin = SecurityUtils.getAuthenticatedUser();

        if(!userLoggin.getId().equals(id)) {
            throw new UnauthorizedException("Você não pode acessar este recurso");
        }
        return userMapper.toResponse(user);
    }
    
}