package com.health.care.healthcare;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

record DoctorRequest(
        @NotBlank String username,
        @NotBlank String name,
        @NotBlank String specialization,
        @NotBlank String licenseNumber,
        @NotNull @DecimalMin("0.00") BigDecimal consultationFee,
        String bio) {}

record ConsultationRequest(
        @NotBlank String doctorId,
        @NotNull ConsultationType type,
        @NotNull @Future Instant scheduledAt) {}

record ConsultationStatusRequest(@NotNull ConsultationStatus status) {}

record PharmacyRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String phone,
        @NotNull @DecimalMin("0.00") BigDecimal commissionRate) {}

record MedicineItemRequest(
        @NotBlank String medicineName,
        @Min(1) int quantity,
        @NotNull @DecimalMin("0.00") BigDecimal unitPrice) {}

record MedicineOrderRequest(
        @NotBlank String pharmacyId,
        @NotEmpty @Valid List<MedicineItemRequest> items,
        @NotBlank String deliveryAddress) {}

record HealthProgramRequest(
        @NotBlank String title,
        @NotBlank String category,
        @NotBlank String content,
        String sponsorName,
        boolean sponsored,
        @NotNull @DecimalMin("0.00") BigDecimal sponsorshipFee) {}

record ReminderRequest(
        @NotNull ReminderType type,
        @NotBlank String title,
        @NotBlank String details,
        @NotNull LocalDate dueDate) {}

record RevenueSummary(
        BigDecimal consultationGross,
        BigDecimal consultationCommission,
        BigDecimal pharmacyGross,
        BigDecimal pharmacyCommission,
        BigDecimal sponsoredProgramRevenue,
        BigDecimal totalRevenue) {}
