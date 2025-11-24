package com.example.tracnghiem.dto.exam;

import java.time.Instant;
import java.util.List;

public record ExamInstanceResponse(
        Long id,
        Long templateId,
        Long studentGroupId,
        String name,
        Instant startTime,
        Instant endTime,
        Integer durationMinutes,
        boolean shuffleQuestions,
        boolean shuffleOptions,
        List<SupervisorPayload> supervisors
) {
    public record SupervisorPayload(Long userId, String fullName, String roomNumber) {
    }
}

