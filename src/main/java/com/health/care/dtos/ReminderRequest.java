package com.health.care.dtos;

import com.health.care.enums.ReminderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReminderRequest(
        @NotNull ReminderType type,
        @NotBlank String title,
        @NotBlank String details,
        @NotNull LocalDate dueDate) {
}
