package com.example.tracnghiem.dto.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateExamTemplateRequest(
        @NotBlank String name,
        @Min(1) Integer totalQuestions,
        @Valid List<ExamStructureRequest> structures
) {
}

