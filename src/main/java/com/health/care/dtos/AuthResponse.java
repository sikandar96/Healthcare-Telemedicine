package com.health.care.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Authentication response containing JWT token")
public record AuthResponse(
        @Schema(description = "JWT token for API authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,
        @Schema(description = "Token type", example = "Bearer")
        String tokenType,
        @Schema(description = "Token expiration time in milliseconds", example = "86400000")
        long expiresIn,
        @Schema(description = "Granted authorities", example = "[ROLE_PATIENT]")
        List<String> roles) {
}
