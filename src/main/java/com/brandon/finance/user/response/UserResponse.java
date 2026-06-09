package com.brandon.finance.user.response;

import com.brandon.finance.user.enums.Role;

public record UserResponse(
    Long id,
    String name,
    String email,
    Role role,
    String oauthProvider,
    String displayPicture,
    String cpf
) {
    public UserResponse(Long id, String name, String email, Role role, String cpf) {
        this(id, name, email, role, null, null, cpf);
    }
}
