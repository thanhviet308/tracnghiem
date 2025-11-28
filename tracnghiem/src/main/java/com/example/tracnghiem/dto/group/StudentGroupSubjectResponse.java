package com.example.tracnghiem.dto.group;

import java.time.Instant;

public record StudentGroupSubjectResponse(
        Long groupId,
        String groupName,
        Long subjectId,
        String subjectName,
        Long teacherId,
        String teacherName,
        Instant assignedAt
) {
}

