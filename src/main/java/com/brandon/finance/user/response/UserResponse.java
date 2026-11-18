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
    @Override
    public String toString() {
        return "UserResponse[id=" + id + ", role=" + role + "]";
    }

    public UserResponse(Long id, String name, String email, Role role, String cpf) {
        this(id, name, email, role, null, null, cpf);
    }
}
