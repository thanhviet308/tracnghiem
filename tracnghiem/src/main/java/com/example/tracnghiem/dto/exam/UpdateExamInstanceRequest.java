package com.example.tracnghiem.dto.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record UpdateExamInstanceRequest(
        @NotNull Long templateId,
        @NotNull Long studentGroupId,
        @NotBlank String name,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @Min(1) Integer durationMinutes,
        @Min(1) Integer totalMarks,
        boolean shuffleQuestions,
        boolean shuffleOptions,
        @Valid List<CreateExamInstanceRequest.SupervisorAssignment> supervisors) {
}

