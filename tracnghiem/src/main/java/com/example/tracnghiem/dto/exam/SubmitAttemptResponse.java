package com.example.tracnghiem.dto.exam;

import java.time.Instant;

public record SubmitAttemptResponse(
        Long attemptId,
        Integer score,
        Instant submittedAt
) {
}

