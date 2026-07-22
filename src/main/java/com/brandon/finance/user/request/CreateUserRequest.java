package com.brandon.finance.user.request;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    String name,
    String email,
    String password,

    @NotBlank(message = "CPF is required")
    @CPF
    String cpf
) {}
