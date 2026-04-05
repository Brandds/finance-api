package com.brandon.finance.auth.dto;

public record LoginRequest (
    String email,
    String password
) {}
