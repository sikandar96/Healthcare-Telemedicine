package com.health.care.dtos;

import jakarta.validation.constraints.NotBlank;

public record OtpRequest(@NotBlank String identifier, String channel) {}
