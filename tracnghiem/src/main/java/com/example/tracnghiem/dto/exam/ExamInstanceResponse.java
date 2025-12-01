package com.example.tracnghiem.dto.exam;

import java.time.Instant;
import java.util.List;

public record ExamInstanceResponse(
        Long id,
        Long templateId,
        Long studentGroupId,
        String name,
        String subjectName,
        Instant startTime,
        Instant endTime,
        Integer durationMinutes,
        Integer totalMarks,
        boolean shuffleQuestions,
        boolean shuffleOptions,
        List<SupervisorPayload> supervisors) {
    public record SupervisorPayload(Long userId, String fullName) {
    }
}
