package com.brandon.finance.auth.service;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.auth.dto.ChangePasswordRequest;
import com.brandon.finance.auth.dto.LoginRequest;
import com.brandon.finance.auth.dto.LoginResponse;
import com.brandon.finance.auth.helper.AuthHelper;
import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;
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
    private final AuthenticatedUserService authenticatedUserService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "As senhas não coincidem");
        }
        if (request.currentPassword().getBytes(StandardCharsets.UTF_8).length > 72
                || request.newPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "A senha deve ter no máximo 72 bytes em UTF-8");
        }

        User user = authenticatedUserService.getUser();
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Usuário não possui senha local");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Senha atual incorreta");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "A nova senha deve ser diferente da atual");
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        authHelper.validarLogin(request, user);

        String token = jwtService.generateToken(user);

        return new LoginResponse(token, userMapper.toResponse(user));
    }
}
