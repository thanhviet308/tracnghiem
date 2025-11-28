package com.example.tracnghiem.dto.user;

import com.example.tracnghiem.domain.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @NotNull UserRole role,
        boolean active,
        String newPassword
) {
}

