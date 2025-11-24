package com.example.tracnghiem.dto.subject;

public record SubjectResponse(
        Long id,
        String name,
        String description,
        boolean active
) {
}

