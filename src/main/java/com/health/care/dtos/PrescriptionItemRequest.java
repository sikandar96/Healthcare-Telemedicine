package com.health.care.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.health.care.enums.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PrescriptionItemRequest(@NotBlank String medicineName, @NotBlank String dosage, @NotBlank String frequency, @Min(1) int durationDays) {}
