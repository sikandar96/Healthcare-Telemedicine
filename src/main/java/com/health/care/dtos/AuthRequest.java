package com.health.care.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Authentication request with username and password")
public record AuthRequest(
        @Schema(description = "Username for authentication", example = "admin")
        @NotBlank String username,
        @Schema(description = "Password for authentication", example = "password123")
        @NotBlank String password) {
}
