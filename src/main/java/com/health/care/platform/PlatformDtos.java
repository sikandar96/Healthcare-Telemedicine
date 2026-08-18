package com.health.care.platform;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

record AppointmentRequest(@NotBlank String doctorId, @NotNull LocalDateTime startAt, @NotNull LocalDateTime endAt) {}
record AppointmentStatusRequest(@NotNull AppointmentStatus status) {}
record VerificationRequest(@NotBlank String username, @NotBlank String licenseNumber) {}
record VerificationDecision(@NotNull VerificationStatus status, String rejectionReason) {}
record ClinicalRecordRequest(@NotBlank String patientUsername, String consultationId, @NotBlank String diagnosis, String notes, List<String> attachmentUrls, boolean patientConsent) {}
record PrescriptionRequest(@NotBlank String patientUsername, String consultationId, @NotEmpty List<@Valid PrescriptionItemRequest> items, String instructions) {}
record PrescriptionItemRequest(@NotBlank String medicineName, @NotBlank String dosage, @NotBlank String frequency, @Min(1) int durationDays) {}
record InventoryRequest(@NotBlank String pharmacyId, @NotBlank String medicineName, @NotBlank String sku, @Min(0) int quantity, @NotNull @DecimalMin("0.00") BigDecimal unitPrice, boolean prescriptionRequired) {}
record InventoryAdjustment(@Min(0) int quantity) {}
record PaymentRequest(@NotBlank String referenceType, @NotBlank String referenceId, @NotNull @DecimalMin("0.01") BigDecimal amount, @NotBlank String currency) {}
record PaymentStatusRequest(@NotNull PaymentStatus status, String providerReference) {}
record ConsentRequest(@NotBlank String grantedTo, @NotBlank String purpose) {}
record NotificationRequest(@NotNull NotificationType type, @NotBlank String title, @NotBlank String message) {}
record CampaignRequest(@NotBlank String sponsor, @NotBlank String title, @NotBlank String description, @NotNull @DecimalMin("0.00") BigDecimal budget, @NotNull LocalDate startDate, @NotNull LocalDate endDate) {}
record PasswordResetRequest(@Email @NotBlank String username) {}
record PasswordResetConfirm(@NotBlank String token, @Size(min = 8) String newPassword) {}
