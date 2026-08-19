package com.health.care.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Authentication request with credentials and optional registration contact details")
public record AuthRequest(
        @Schema(description = "Username, email address, or mobile number used for authentication", example = "ananya@example.com")
        @NotBlank String username,
        @Schema(description = "Password for authentication", example = "password123")
        @NotBlank String password,
        @Schema(description = "Registration role. Privileged roles must be provisioned by an administrator", example = "PATIENT")
        String role,
        @Schema(description = "Email address used for account recovery", example = "ananya@example.com")
        @Email String email,
        @Schema(description = "Mobile number used for account recovery and login", example = "+919876543210")
        String phone,
        @Schema(description = "User full name", example = "Ananya Sharma")
        String fullName) {
    public AuthRequest(String username, String password) {
        this(username, password, null, null, null, null);
    }
}
