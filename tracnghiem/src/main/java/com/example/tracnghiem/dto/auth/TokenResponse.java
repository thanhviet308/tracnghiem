package com.example.tracnghiem.dto.auth;

import com.example.tracnghiem.domain.user.UserRole;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserRole role,
        Long userId,
        String fullName
) {
}

