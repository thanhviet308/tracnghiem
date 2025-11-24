package com.example.tracnghiem.exception;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        String message,
        List<String> details
) {
    public static ApiError of(String message, List<String> details) {
        return new ApiError(Instant.now(), message, details);
    }
}

