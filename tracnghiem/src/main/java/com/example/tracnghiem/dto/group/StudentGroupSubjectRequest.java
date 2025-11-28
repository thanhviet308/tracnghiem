package com.example.tracnghiem.dto.group;

import jakarta.validation.constraints.NotNull;

public record StudentGroupSubjectRequest(
        @NotNull Long groupId,
        @NotNull Long subjectId,
        @NotNull Long teacherId
) {
}

