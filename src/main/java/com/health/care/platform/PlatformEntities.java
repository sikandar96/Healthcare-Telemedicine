package com.health.care.platform;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document("doctor_verifications")
@Data @NoArgsConstructor @AllArgsConstructor
class DoctorVerification {
    @Id private String id;
    private String username;
    private String licenseNumber;
    private VerificationStatus status;
    private String reviewer;
    private String rejectionReason;
    private Instant submittedAt;
    private Instant reviewedAt;
}

enum VerificationStatus { PENDING, APPROVED, REJECTED }

@Document("appointments")
@Data @NoArgsConstructor @AllArgsConstructor
class Appointment {
    @Id private String id;
    private String patientUsername;
    private String doctorId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private AppointmentStatus status;
    private String consultationId;
    private Instant createdAt;
}

enum AppointmentStatus { REQUESTED, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW }

@Document("clinical_records")
@Data @NoArgsConstructor @AllArgsConstructor
class ClinicalRecord {
    @Id private String id;
    private String patientUsername;
    private String doctorUsername;
    private String consultationId;
    private String diagnosis;
    private String notes;
    private List<String> attachmentUrls;
    private boolean patientConsent;
    private Instant createdAt;
    private Instant updatedAt;
}

@Document("prescriptions")
@Data @NoArgsConstructor @AllArgsConstructor
class Prescription {
    @Id private String id;
    private String patientUsername;
    private String doctorUsername;
    private String consultationId;
    private List<PrescriptionItem> items;
    private String instructions;
    private PrescriptionStatus status;
    private Instant issuedAt;
}

@Data @NoArgsConstructor @AllArgsConstructor
class PrescriptionItem {
    private String medicineName;
    private String dosage;
    private String frequency;
    private int durationDays;
}

enum PrescriptionStatus { ACTIVE, DISPENSED, CANCELLED }

@Document("pharmacy_inventory")
@Data @NoArgsConstructor @AllArgsConstructor
class PharmacyInventory {
    @Id private String id;
    private String pharmacyId;
    private String medicineName;
    private String sku;
    private int quantity;
    private BigDecimal unitPrice;
    private boolean prescriptionRequired;
    private Instant updatedAt;
}

@Document("payment_transactions")
@Data @NoArgsConstructor @AllArgsConstructor
class PaymentTransaction {
    @Id private String id;
    private String payerUsername;
    private String referenceType;
    private String referenceId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String providerReference;
    private Instant createdAt;
    private Instant updatedAt;
}

enum PaymentStatus { CREATED, PENDING, PAID, FAILED, REFUNDED }

@Document("notifications")
@Data @NoArgsConstructor @AllArgsConstructor
class Notification {
    @Id private String id;
    private String username;
    private NotificationType type;
    private String title;
    private String message;
    private boolean read;
    private Instant createdAt;
}

enum NotificationType { APPOINTMENT, PRESCRIPTION, PAYMENT, ORDER, REMINDER, SYSTEM }

@Document("audit_events")
@Data @NoArgsConstructor @AllArgsConstructor
class AuditEvent {
    @Id private String id;
    private String actor;
    private String action;
    private String entityType;
    private String entityId;
    private String correlationId;
    private String outcome;
    private Instant occurredAt;
}

@Document("consents")
@Data @NoArgsConstructor @AllArgsConstructor
class ConsentRecord {
    @Id private String id;
    private String patientUsername;
    private String grantedTo;
    private String purpose;
    private boolean active;
    private Instant grantedAt;
    private Instant revokedAt;
}

@Document("password_reset_tokens")
@Data @NoArgsConstructor @AllArgsConstructor
class PasswordResetToken {
    @Id private String id;
    private String username;
    private String tokenHash;
    private Instant expiresAt;
    private boolean used;
}

@Document("wellness_campaigns")
@Data @NoArgsConstructor @AllArgsConstructor
class WellnessCampaign {
    @Id private String id;
    private String sponsor;
    private String title;
    private String description;
    private BigDecimal budget;
    private LocalDate startDate;
    private LocalDate endDate;
    private CampaignStatus status;
    private Instant createdAt;
}

enum CampaignStatus { DRAFT, ACTIVE, PAUSED, COMPLETED }
