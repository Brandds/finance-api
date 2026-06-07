package com.brandon.finance.auth.dto;

import com.brandon.finance.user.response.UserResponse;

public record LoginResponse (
    String token,
    UserResponse user
) {}
