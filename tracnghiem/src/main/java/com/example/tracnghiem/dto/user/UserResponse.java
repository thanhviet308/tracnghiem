package com.example.tracnghiem.dto.user;

import com.example.tracnghiem.domain.user.UserRole;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        UserRole role,
        boolean active
) {
}

