package com.brandon.finance.auth.model;

import com.brandon.finance.user.enums.Role;

public record AuthenticatedUser(
    Long id,
    String email,
    Role role
) {

}
