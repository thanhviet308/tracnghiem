package com.example.tracnghiem.dto.exam;

import java.time.Instant;

public record ViolationResponse(
    Long id,
    Long attemptId,
    Long studentId,
    String studentName,
    String violationType,
    Integer violationCount,
    Instant lastOccurredAt
) {}

