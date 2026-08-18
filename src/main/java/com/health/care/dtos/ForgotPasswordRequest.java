package com.health.care.dtos;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(@NotBlank String identifier) {}
