package com.careerpilot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerEvaluationRequest(
        @NotBlank @Size(max = 2000) String question,
        @NotBlank @Size(max = 10000) String answer,
        Long jobId) {
}
