package com.health.care.healthcare;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class HealthcareEntities {
    private HealthcareEntities() {}
}

@Document("doctors")
@Data @NoArgsConstructor @AllArgsConstructor
class DoctorProfile {
    @Id private String id;
    private String username;
    private String name;
    private String specialization;
    private String licenseNumber;
    private boolean certified;
    private boolean available;
    private BigDecimal consultationFee;
    private String bio;
}

@Document("consultations")
@Data @NoArgsConstructor @AllArgsConstructor
class Consultation {
    @Id private String id;
    private String patientUsername;
    private String doctorId;
    private String doctorName;
    private ConsultationType type;
    private ConsultationStatus status;
    private Instant scheduledAt;
    private Instant startedAt;
    private Instant completedAt;
    private BigDecimal fee;
    private BigDecimal platformCommission;
    private BigDecimal doctorPayout;
    private String meetingRoomUrl;
    private Instant createdAt;
}

@Document("pharmacies")
@Data @NoArgsConstructor @AllArgsConstructor
class Pharmacy {
    @Id private String id;
    private String name;
    private String address;
    private String phone;
    private boolean verified;
    private boolean active;
    private BigDecimal commissionRate;
}

@Data @NoArgsConstructor @AllArgsConstructor
class MedicineOrderItem {
    private String medicineName;
    private int quantity;
    private BigDecimal unitPrice;
}

@Document("medicine_orders")
@Data @NoArgsConstructor @AllArgsConstructor
class MedicineOrder {
    @Id private String id;
    private String patientUsername;
    private String pharmacyId;
    private String pharmacyName;
    private List<MedicineOrderItem> items;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal total;
    private BigDecimal pharmacyCommission;
    private MedicineOrderStatus status;
    private String deliveryAddress;
    private Instant createdAt;
    private Instant updatedAt;
}

@Document("health_programs")
@Data @NoArgsConstructor @AllArgsConstructor
class HealthProgram {
    @Id private String id;
    private String title;
    private String category;
    private String content;
    private String sponsorName;
    private boolean sponsored;
    private BigDecimal sponsorshipFee;
    private boolean published;
    private Instant publishedAt;
}

@Document("preventive_reminders")
@Data @NoArgsConstructor @AllArgsConstructor
class PreventiveReminder {
    @Id private String id;
    private String username;
    private ReminderType type;
    private String title;
    private String details;
    private LocalDate dueDate;
    private boolean completed;
    private boolean notified;
    private Instant createdAt;
}

enum ConsultationType { VIDEO, AUDIO }
enum ConsultationStatus { REQUESTED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED }
enum MedicineOrderStatus { PLACED, ACCEPTED, PACKED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED }
enum ReminderType { VACCINATION, CHECKUP, MEDICATION, SCREENING }
