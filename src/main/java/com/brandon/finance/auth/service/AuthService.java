package com.brandon.finance.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.brandon.finance.auth.dto.LoginRequest;
import com.brandon.finance.auth.dto.LoginResponse;
import com.brandon.finance.auth.helper.AuthHelper;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.mapper.UserMapper;
import com.brandon.finance.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuthHelper authHelper;

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        authHelper.validarLogin(request, user);

        String token = jwtService.generateToken(user);

        return new LoginResponse(token, userMapper.toResponse(user));
    }
}
