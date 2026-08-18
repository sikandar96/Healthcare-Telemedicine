package com.health.care.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PharmacyRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String phone,
        @NotNull @DecimalMin("0.00") BigDecimal commissionRate) {
}
