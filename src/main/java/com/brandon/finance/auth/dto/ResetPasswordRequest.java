package com.brandon.finance.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank @Size(max = 128) String token,
    @NotBlank @Size(min = 8, max = 72) String newPassword,
    @NotBlank String confirmPassword
) {
    @Override
    public String toString() {
        return "ResetPasswordRequest[REDACTED]";
    }
}
