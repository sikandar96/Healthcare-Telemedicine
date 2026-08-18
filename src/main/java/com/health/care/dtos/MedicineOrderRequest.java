package com.health.care.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MedicineOrderRequest(
        @NotBlank String pharmacyId,
        @NotEmpty @Valid List<MedicineItemRequest> items,
        @NotBlank String deliveryAddress) {
}
