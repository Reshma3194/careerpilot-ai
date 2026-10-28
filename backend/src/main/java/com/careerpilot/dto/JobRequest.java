package com.careerpilot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String company,
        @Size(max = 255) String location,
        @Size(max = 20000) String description,
        @Size(max = 1000) String jobUrl) {
}
