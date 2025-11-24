package com.example.tracnghiem.dto.subject;

import jakarta.validation.constraints.NotBlank;

public record SubjectRequest(
        @NotBlank String name,
        String description,
        boolean active
) {
}

