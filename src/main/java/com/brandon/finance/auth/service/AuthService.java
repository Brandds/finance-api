package com.brandon.finance.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.brandon.finance.auth.dto.LoginRequest;
import com.brandon.finance.auth.dto.LoginResponse;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.shared.base.excepetion.UnauthorizedException;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(token);
    }
}
