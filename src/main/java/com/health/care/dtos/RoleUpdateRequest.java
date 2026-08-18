package com.health.care.dtos;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RoleUpdateRequest(
        @NotEmpty(message = "At least one role is required")
        List<String> roles) {
}
