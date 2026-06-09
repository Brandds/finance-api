package com.brandon.finance.user.request;

public record EditUserRequest(
    String name,
    String email,
    String cpf
) {}
