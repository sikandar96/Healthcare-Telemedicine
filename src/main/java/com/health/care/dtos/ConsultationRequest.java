package com.health.care.dtos;

import com.health.care.enums.ConsultationType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ConsultationRequest(
        @NotBlank String doctorId,
        @NotNull ConsultationType type,
        @NotNull @Future Instant scheduledAt) {
}
