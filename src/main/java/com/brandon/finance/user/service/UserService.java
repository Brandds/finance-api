package com.brandon.finance.user.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.email.service.impl.EmailServiceImpl;
import com.brandon.finance.email.service.impl.EmailVerificationTokenServiceImpl;
import com.brandon.finance.helper.TokenUtils;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.user.entity.User;
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
    private final EmailVerificationTokenServiceImpl emailVerificationTokenService;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        
        String encodedPassword = passwordEncoder.encode(request.password());
        var user = userMapper.toEntity(request, encodedPassword);
        var savedUser = userRepository.save(user);

        String tokenEmail = TokenUtils.generateToken();
        emailVerificationTokenService.create(tokenEmail, savedUser);
        emailService.sendVerificationEmail(savedUser, tokenEmail);

        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(getOwnedUser(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Transactional
    public UserResponse updateUserLogged(UserEditResponse request) {
        return updateUser(authenticatedUserService.getUserId(), request);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserEditResponse request) {
        var user = getOwnedUser(id);
        userRepository.save(userMapper.toEntityEdit(user, request));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return getUserById(authenticatedUserService.getUserId());
    }

    private User getOwnedUser(Long id) {
        if (!authenticatedUserService.getUserId().equals(id)) {
            throw new AuthorizationDeniedException("Você não pode acessar este recurso");
        }
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario não encontrado"));
    }
}
