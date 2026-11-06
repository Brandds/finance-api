package com.brandon.finance.user.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserEditResponse(
    @NotBlank(message = "Nome obrigatório")
    String name,
    @NotBlank(message = "E-mail obrigatório")
    @Email(message = "E-mail inválido")
    String email
) {
    
}
