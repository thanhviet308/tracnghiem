package com.example.tracnghiem.dto.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PassageRequest(
        @NotNull Long chapterId,
        @NotBlank String content
) {
}

