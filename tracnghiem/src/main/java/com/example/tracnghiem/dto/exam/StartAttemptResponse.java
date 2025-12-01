package com.example.tracnghiem.dto.exam;

import com.example.tracnghiem.domain.exam.ExamAttemptStatus;

import java.time.Instant;
import java.util.List;

public record StartAttemptResponse(
        Long attemptId,
        Long examInstanceId,
        ExamAttemptStatus status,
        Instant startedAt,
        Instant expiresAt,
        List<ExamQuestionView> questions
) {
    public record ExamQuestionView(
            Long questionId,
            String content,
            String questionType,
            Integer marks,
            Long passageId,
            String passageContent,
            List<OptionView> options
    ) {
    }

    public record OptionView(Long optionId, String content) {
    }
}

