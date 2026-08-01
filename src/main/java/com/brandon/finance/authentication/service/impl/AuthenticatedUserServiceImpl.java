package com.brandon.finance.authentication.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.brandon.finance.auth.model.AuthenticatedUser;
import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.shared.base.excepetion.UnauthorizedException;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserServiceImpl implements AuthenticatedUserService {
    
    private final UserRepository userRepository;

    @Override
    public AuthenticatedUser getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

       if (!(principal instanceof AuthenticatedUser authenticatedUser)) {
            throw new UnauthorizedException("Usuário não autenticado.");
        }

        return authenticatedUser;
    }

    @Override
    public Long getUserId() {
        return getAuthenticatedUser().id();
    }

    @Override
    public String getEmail() {
        return getAuthenticatedUser().email();
    }

    @Override
    public User getUser() {
        return userRepository.findById(getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário autenticado não encontrado."));
    }

}
