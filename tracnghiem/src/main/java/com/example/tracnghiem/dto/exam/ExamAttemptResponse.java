package com.example.tracnghiem.dto.exam;

import com.example.tracnghiem.domain.exam.ExamAttemptStatus;

import java.time.Instant;

public record ExamAttemptResponse(
        Long attemptId,
        Long examInstanceId,
        Long studentId,
        String studentName,
        String studentEmail,
        Instant startedAt,
        Instant submittedAt,
        Integer score,
        ExamAttemptStatus status
) {
}

