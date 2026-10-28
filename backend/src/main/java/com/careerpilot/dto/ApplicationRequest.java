package com.careerpilot.dto;

import com.careerpilot.entity.JobApplication;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ApplicationRequest(
        @NotNull Long jobId,
        JobApplication.Status status,
        LocalDate appliedDate,
        String notes) {
}
