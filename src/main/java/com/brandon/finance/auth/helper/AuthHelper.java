package com.brandon.finance.auth.helper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.brandon.finance.auth.dto.LoginRequest;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;
import com.brandon.finance.shared.base.excepetion.UnauthorizedException;
import com.brandon.finance.user.entity.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class AuthHelper {

    private final PasswordEncoder passwordEncoder;

    
    public  void validarLogin(LoginRequest request, User user){
        validarCredenciais(request, user);
        validarUsuarioHabilitado(user);
    }

    private void validarCredenciais(LoginRequest request, User user) {
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Credenciais inválidas");
        }
    }

    private void validarUsuarioHabilitado(User user) {
        if (!user.isEnabled()) {
            throw new BusinessException(
                ErrorCode.EMAIL_NOT_VERIFIED,
                "Você preicsa verificar seu e-mail antes de fazer login. Por favor, verifique sua caixa de entrada para o e-mail de verificação."
            );
        }
    }
}
