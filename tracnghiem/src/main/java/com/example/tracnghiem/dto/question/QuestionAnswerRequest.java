package com.example.tracnghiem.dto.question;

import jakarta.validation.constraints.NotBlank;

public record QuestionAnswerRequest(
        @NotBlank String answer
) {
}

