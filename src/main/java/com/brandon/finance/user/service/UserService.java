package com.brandon.finance.user.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.brandon.finance.email.service.EmailServiceImpl;
import com.brandon.finance.email.service.EmailVerificationTokenService;
import com.brandon.finance.helper.SecurityUtils;
import com.brandon.finance.helper.TokenUtils;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.shared.base.excepetion.UnauthorizedException;
import com.brandon.finance.user.mapper.UserMapper;
import com.brandon.finance.user.repository.UserRepository;
import com.brandon.finance.user.request.CreateUserRequest;
import com.brandon.finance.user.response.UserEditResponse;
import com.brandon.finance.user.response.UserResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceImpl emailService;
    private final EmailVerificationTokenService emailVerificationTokenService;

    public UserResponse create(CreateUserRequest request) {
        
        String encodedPassword = passwordEncoder.encode(request.password());
        var user = userMapper.toEntity(request, encodedPassword);
        var savedUser = userRepository.save(user);

        String tokenEmail = TokenUtils.generateToken();
        emailVerificationTokenService.create(tokenEmail, savedUser);
        emailService.sendVerificationEmail(savedUser, tokenEmail);

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

    public List<UserResponse> getAllUsers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllUsers'");
    }

    public UserResponse updateUserLogged(UserEditResponse request) {
        var userLoggin = SecurityUtils.getAuthenticatedUser();
        userRepository.save(userMapper.toEntityEdit(userLoggin, request));
        return userMapper.toResponseEdit(userLoggin, request);
    }

    public UserResponse updateUser(Long id, UserEditResponse request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    public UserResponse getCurrentUser() {
        var userLoggin = SecurityUtils.getAuthenticatedUser();
        var user = userRepository.findById(userLoggin.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario não encontrado"));
        return userMapper.toResponse(user);
    }


    
}