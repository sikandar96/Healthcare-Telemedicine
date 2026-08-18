package com.health.care.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Authentication request with credentials and optional registration contact details")
public record AuthRequest(
        @Schema(description = "Username for authentication", example = "admin")
        @NotBlank String username,
        @Schema(description = "Password for authentication", example = "password123")
        @NotBlank String password,
        @Schema(description = "Registration role. Privileged roles must be provisioned by an administrator", example = "PATIENT")
        String role,
        @Schema(description = "Email address used for account recovery", example = "ananya@example.com")
        @Email String email,
        @Schema(description = "Indian mobile number used for account recovery", example = "+919876543210")
        String phone) {
    public AuthRequest(String username, String password) {
        this(username, password, null, null, null);
    }
}
