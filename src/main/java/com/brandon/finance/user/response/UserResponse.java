package com.brandon.finance.user.response;

import com.brandon.finance.user.enums.Role;

public record UserResponse(
    Long id,
    String name,
    String email,
    Role role
) {}
