package com.health.care.services;

import com.health.care.dtos.*;
import com.health.care.entities.*;
import com.health.care.enums.*;

import java.util.List;

/**
 * Application-facing contract for platform use cases. Controllers depend on
 * this abstraction, while persistence and orchestration remain in the service
 * implementation.
 */
public interface PlatformOperations {
    DoctorVerification submitVerification(VerificationRequest request, String actor);
    List<DoctorVerification> pendingVerifications();
    DoctorVerification decideVerification(String id, VerificationDecision decision, String reviewer);

    Appointment bookAppointment(String patient, AppointmentRequest request);
    List<Appointment> patientAppointments(String username);
    List<Appointment> doctorAppointments(String doctorId);
    List<Appointment> doctorAppointmentsForUser(String username);
    Appointment updateAppointment(String id, AppointmentStatus status, String actor);

    ClinicalRecord createClinicalRecord(String doctor, ClinicalRecordRequest request);
    List<ClinicalRecord> patientRecords(String patient);
    List<ClinicalRecord> doctorRecords(String doctor);

    Prescription createPrescription(String doctor, PrescriptionRequest request);
    List<Prescription> patientPrescriptions(String patient);
    List<Prescription> doctorPrescriptions(String doctor);

    PharmacyInventory upsertInventory(InventoryRequest request, String actor);
    PharmacyInventory adjustInventory(String id, int quantity, String actor);
    List<PharmacyInventory> pharmacyInventory(String pharmacyId);

    PaymentTransaction createPayment(String payer, PaymentRequest request, String idempotencyKey);
    PaymentTransaction updatePayment(String id, PaymentStatus status, String providerReference, String actor);
    List<PaymentTransaction> payments(String username);

    Notification notifyUser(String username, NotificationType type, String title, String message);
    List<Notification> notifications(String username);
    Notification markNotificationRead(String id, String username);

    ConsentRecord grantConsent(String patient, ConsentRequest request);
    ConsentRecord revokeConsent(String id, String patient);
    List<ConsentRecord> activeConsents(String patient);

    WellnessCampaign createCampaign(CampaignRequest request, String actor);
    List<WellnessCampaign> activeCampaigns();

    void audit(String actor, String action, String entityType, String entityId, String outcome);
    long auditCount(String action);
}
