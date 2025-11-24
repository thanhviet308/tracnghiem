package com.example.tracnghiem.dto.question;

import jakarta.validation.constraints.NotBlank;

public record QuestionOptionRequest(
        @NotBlank String content,
        boolean correct
) {
}

