package com.health.care.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.health.care.enums.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InventoryRequest(@NotBlank String pharmacyId, @NotBlank String medicineName, @NotBlank String sku, @Min(0) int quantity, @NotNull @DecimalMin("0.00") BigDecimal unitPrice, boolean prescriptionRequired) {}
