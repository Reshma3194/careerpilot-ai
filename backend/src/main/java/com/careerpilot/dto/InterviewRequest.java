package com.careerpilot.dto;

import jakarta.validation.constraints.NotNull;

public record InterviewRequest(
        @NotNull Long jobId,
        Long resumeId,
        Integer count) {
}
