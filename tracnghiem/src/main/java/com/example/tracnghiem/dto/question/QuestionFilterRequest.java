package com.example.tracnghiem.dto.question;

import com.example.tracnghiem.domain.question.QuestionType;

public record QuestionFilterRequest(
        Long subjectId,
        Long chapterId,
        String difficulty,
        Long createdBy,
        Boolean hasPassage,
        QuestionType questionType
) {
}

