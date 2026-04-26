package com.brandon.finance.auth.google.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
    @NotBlank(message = "O código de autorização é obrigatório")
    String code
) {
    
}
