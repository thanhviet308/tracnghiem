package com.example.tracnghiem.dto.exam;

import java.util.List;

public record ExamTemplateResponse(
        Long id,
        Long subjectId,
        String name,
        Integer totalQuestions,
        List<ExamStructurePayload> structures) {
    public record ExamStructurePayload(Long id, Long chapterId, Integer numQuestion, Integer numBasic,
            Integer numAdvanced) {
    }
}
