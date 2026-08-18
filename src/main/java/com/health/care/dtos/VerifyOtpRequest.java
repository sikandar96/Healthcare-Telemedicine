package com.health.care.dtos;

import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequest(@NotBlank String identifier, @NotBlank String otp) {}
