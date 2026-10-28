package com.careerpilot.dto;

import com.careerpilot.entity.JobApplication;

import java.time.LocalDate;

/** Partial update: any field left null is not changed. */
public record ApplicationUpdateRequest(
        JobApplication.Status status,
        LocalDate appliedDate,
        String notes) {
}
