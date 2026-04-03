package com.brandon.finance.user.request;


public record CreateUserRequest(
    String name,
    String email,
    String password
) {}
