package com.example.tracnghiem.dto.group;

import java.time.Instant;

public record StudentGroupResponse(
        Long id,
        String name,
        Instant createdAt,
        int numberOfStudents
) {
}


