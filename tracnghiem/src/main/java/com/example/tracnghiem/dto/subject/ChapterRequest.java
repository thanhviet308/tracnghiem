package com.example.tracnghiem.dto.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChapterRequest(
        @NotNull Long subjectId,
        @NotBlank String name,
        String description
) {
}

