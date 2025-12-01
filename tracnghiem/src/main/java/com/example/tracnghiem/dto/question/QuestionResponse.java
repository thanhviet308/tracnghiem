package com.example.tracnghiem.dto.question;

import com.example.tracnghiem.domain.question.QuestionType;

import java.util.List;

public record QuestionResponse(
        Long id,
        Long chapterId,
        Long passageId,
        String content,
        QuestionType questionType,
        String difficulty,
        boolean active,
        List<QuestionOptionPayload> options,
        List<String> answers) {
    public record QuestionOptionPayload(Long id, String content, boolean correct) {
    }
}
