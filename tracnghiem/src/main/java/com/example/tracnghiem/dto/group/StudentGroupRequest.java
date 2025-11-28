package com.example.tracnghiem.dto.group;

import jakarta.validation.constraints.NotBlank;

public record StudentGroupRequest(
        @NotBlank String name
) {
}
