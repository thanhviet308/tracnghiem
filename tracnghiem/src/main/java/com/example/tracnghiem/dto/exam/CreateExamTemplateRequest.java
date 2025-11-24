package com.example.tracnghiem.dto.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateExamTemplateRequest(
        @NotNull Long subjectId,
        @NotBlank String name,
        @Min(1) Integer totalQuestions,
        @Min(1) Integer durationMinutes,
        @Valid List<ExamStructureRequest> structures
) {
}

