package com.example.tracnghiem.dto.subject;

public record ChapterResponse(
        Long id,
        Long subjectId,
        String name,
        String description
) {
}

