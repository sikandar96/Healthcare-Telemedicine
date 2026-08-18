package com.health.care.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record HealthProgramRequest(
        @NotBlank String title,
        @NotBlank String category,
        @NotBlank String content,
        String sponsorName,
        boolean sponsored,
        @NotNull @DecimalMin("0.00") BigDecimal sponsorshipFee) {
}
