package com.health.care.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DoctorRequest(
        @NotBlank String username,
        @NotBlank String name,
        @NotBlank String specialization,
        @NotBlank String licenseNumber,
        @NotNull @DecimalMin("0.00") BigDecimal consultationFee,
        String bio) {
}
