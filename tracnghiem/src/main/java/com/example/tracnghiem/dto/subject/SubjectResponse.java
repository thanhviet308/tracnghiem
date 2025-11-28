package com.example.tracnghiem.dto.subject;

import java.time.Instant;

public record SubjectResponse(
        Long id,
        String name,
        String description,
        boolean active,
        Instant createdAt
) {
}

