package com.brandon.finance.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.brandon.finance.auth.dto.ForgotPasswordRequest;
import com.brandon.finance.auth.dto.ResetPasswordRequest;
import com.brandon.finance.auth.service.PasswordResetService;
import com.brandon.finance.shared.base.response.ApiResponse;
import com.brandon.finance.shared.base.response.ResponseUtil;
import com.brandon.finance.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.AUTH)
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService service;

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperação de senha por e-mail")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        service.requestReset(request.email());
        return ResponseUtil.ok(null, "Se o e-mail pertencer a uma conta elegível, as instruções serão enviadas");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha com token de recuperação")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        service.resetPassword(request);
        return ResponseUtil.ok(null, "Senha redefinida com sucesso");
    }
}
