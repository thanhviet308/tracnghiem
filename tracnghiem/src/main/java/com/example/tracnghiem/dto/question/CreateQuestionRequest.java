package com.example.tracnghiem.dto.question;

import com.example.tracnghiem.domain.question.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateQuestionRequest(
        @NotNull Long chapterId,
        Long passageId,
        @NotBlank String content,
        @NotNull QuestionType questionType,
        String difficulty,
        @Min(1) Integer marks,
        boolean active,
        @Valid List<QuestionOptionRequest> options,
        @Valid List<QuestionAnswerRequest> answers
) {
}

