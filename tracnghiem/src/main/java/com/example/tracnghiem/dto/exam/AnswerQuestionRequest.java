package com.example.tracnghiem.dto.exam;

import jakarta.validation.constraints.NotNull;

public record AnswerQuestionRequest(
        @NotNull Long questionId,
        Long selectedOptionId,
        String fillAnswer
) {
}

