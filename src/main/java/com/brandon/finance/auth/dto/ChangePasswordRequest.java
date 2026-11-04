package com.brandon.finance.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "Senha atual obrigatória") String currentPassword,
    @NotBlank(message = "Nova senha obrigatória")
    @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres") String newPassword,
    @NotBlank(message = "Confirmação de senha obrigatória") String confirmPassword
) {}
