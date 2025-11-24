package com.example.tracnghiem.dto.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record CreateExamInstanceRequest(
        @NotNull Long templateId,
        @NotNull Long studentGroupId,
        @NotBlank String name,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @Min(1) Integer durationMinutes,
        boolean shuffleQuestions,
        boolean shuffleOptions,
        @Valid List<SupervisorAssignment> supervisors
) {
    public record SupervisorAssignment(@NotNull Long supervisorId, String roomNumber) {
    }
}

