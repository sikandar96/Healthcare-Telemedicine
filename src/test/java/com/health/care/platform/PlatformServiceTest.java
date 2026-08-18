package com.health.care.platform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformServiceTest {
    @Mock DoctorVerificationRepository verifications;
    @Mock com.health.care.repositories.DoctorRepository doctors;
    @Mock AppointmentRepository appointments;
    @Mock ClinicalRecordRepository clinicalRecords;
    @Mock PrescriptionRepository prescriptions;
    @Mock PharmacyInventoryRepository inventory;
    @Mock PaymentTransactionRepository payments;
    @Mock NotificationRepository notifications;
    @Mock AuditEventRepository audits;
    @Mock ConsentRecordRepository consents;
    @Mock WellnessCampaignRepository campaigns;

    private PlatformService service;

    @BeforeEach
    void setUp() {
        service = new PlatformService(verifications, doctors, appointments, clinicalRecords, prescriptions, inventory,
                payments, notifications, audits, consents, campaigns);
    }

    @Test
    void rejectsOverlappingAppointment() {
        when(appointments.existsByDoctorIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(any(), any(), any(), any())).thenReturn(true);
        AppointmentRequest request = new AppointmentRequest("doctor-1", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
        assertThrows(IllegalArgumentException.class, () -> service.bookAppointment("patient", request));
    }

    @Test
    void rejectsClinicalRecordWithoutConsent() {
        ClinicalRecordRequest request = new ClinicalRecordRequest("patient", "consultation", "diagnosis", "notes", List.of(), false);
        assertThrows(IllegalArgumentException.class, () -> service.createClinicalRecord("doctor", request));
    }

    @Test
    void rejectsRevokingAnotherPatientsConsent() {
        ConsentRecord consent = new ConsentRecord("consent-1", "patient-a", "doctor", "consultation", true, null, null);
        when(consents.findById("consent-1")).thenReturn(Optional.of(consent));
        assertThrows(IllegalArgumentException.class, () -> service.revokeConsent("consent-1", "patient-b"));
    }
}
