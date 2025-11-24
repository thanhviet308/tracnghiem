package com.example.tracnghiem.dto.exam;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExamStructureRequest(
        @NotNull Long chapterId,
        @Min(1) Integer numQuestion
) {
}

