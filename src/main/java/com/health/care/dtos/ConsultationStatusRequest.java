package com.health.care.dtos;

import com.health.care.enums.ConsultationStatus;
import jakarta.validation.constraints.NotNull;

public record ConsultationStatusRequest(@NotNull ConsultationStatus status) {
}
