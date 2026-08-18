package com.health.care.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.health.care.enums.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentRequest(@NotBlank String referenceType, @NotBlank String referenceId, @NotNull @DecimalMin("0.01") BigDecimal amount, @NotBlank String currency) {}
