package com.example.tracnghiem.dto.exam;

import jakarta.validation.constraints.NotBlank;

public record ViolationRequest(
    @NotBlank String violationType
) {}

