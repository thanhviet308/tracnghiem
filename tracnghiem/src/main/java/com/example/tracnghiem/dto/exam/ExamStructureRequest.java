package com.example.tracnghiem.dto.exam;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExamStructureRequest(
                @NotNull Long chapterId,
                @Min(1) Integer numQuestion,
                @Min(0) Integer numBasic, // Số câu hỏi cơ bản
                @Min(0) Integer numAdvanced // Số câu hỏi nâng cao
) {
}
