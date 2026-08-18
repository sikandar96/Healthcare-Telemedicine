package com.health.care.platform;

import com.health.care.repositories.DoctorRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PlatformService {
    private static final Logger logger = LoggerFactory.getLogger(PlatformService.class);
    private final DoctorVerificationRepository verifications;
    private final DoctorRepository doctors;
    private final AppointmentRepository appointments;
    private final ClinicalRecordRepository clinicalRecords;
    private final PrescriptionRepository prescriptions;
    private final PharmacyInventoryRepository inventory;
    private final PaymentTransactionRepository payments;
    private final NotificationRepository notifications;
    private final AuditEventRepository audits;
    private final ConsentRecordRepository consents;
    private final WellnessCampaignRepository campaigns;

    public PlatformService(DoctorVerificationRepository verifications, DoctorRepository doctors, AppointmentRepository appointments,
                           ClinicalRecordRepository clinicalRecords, PrescriptionRepository prescriptions,
                           PharmacyInventoryRepository inventory, PaymentTransactionRepository payments,
                           NotificationRepository notifications, AuditEventRepository audits,
                           ConsentRecordRepository consents, WellnessCampaignRepository campaigns) {
        this.verifications = verifications;
        this.doctors = doctors;
        this.appointments = appointments;
        this.clinicalRecords = clinicalRecords;
        this.prescriptions = prescriptions;
        this.inventory = inventory;
        this.payments = payments;
        this.notifications = notifications;
        this.audits = audits;
        this.consents = consents;
        this.campaigns = campaigns;
    }

    public DoctorVerification submitVerification(VerificationRequest request, String actor) {
        if (!actor.equals(request.username())) {
            throw new IllegalArgumentException("Users may submit verification only for their own account");
        }
        DoctorVerification verification = new DoctorVerification(null, request.username(), request.licenseNumber(),
                VerificationStatus.PENDING, null, null, Instant.now(), null);
        DoctorVerification saved = verifications.save(verification);
        audit(actor, "DOCTOR_VERIFICATION_SUBMITTED", "DoctorVerification", saved.getId(), "SUCCESS");
        logger.info("Doctor verification '{}' submitted for user '{}'", saved.getId(), request.username());
        return saved;
    }

    public List<DoctorVerification> pendingVerifications() {
        return verifications.findByStatusOrderBySubmittedAtAsc(VerificationStatus.PENDING);
    }

    public DoctorVerification decideVerification(String id, VerificationDecision decision, String reviewer) {
        DoctorVerification verification = verifications.findById(id).orElseThrow(() -> new IllegalArgumentException("Verification not found"));
        if (verification.getStatus() != VerificationStatus.PENDING) throw new IllegalArgumentException("Verification has already been decided");
        if (decision.status() == VerificationStatus.REJECTED && (decision.rejectionReason() == null || decision.rejectionReason().isBlank())) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        verification.setStatus(decision.status());
        verification.setReviewer(reviewer);
        verification.setRejectionReason(decision.rejectionReason());
        verification.setReviewedAt(Instant.now());
        DoctorVerification saved = verifications.save(verification);
        audit(reviewer, "DOCTOR_VERIFICATION_" + decision.status(), "DoctorVerification", id, "SUCCESS");
        return saved;
    }

    @Transactional
    public Appointment bookAppointment(String patient, AppointmentRequest request) {
        if (!request.endAt().isAfter(request.startAt())) throw new IllegalArgumentException("Appointment end must be after start");
        if (appointments.existsByDoctorIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(request.doctorId(),
                List.of(AppointmentStatus.REQUESTED, AppointmentStatus.CONFIRMED), request.endAt(), request.startAt())) {
            throw new IllegalArgumentException("Doctor is not available for the requested slot");
        }
        Appointment appointment = new Appointment(null, patient, request.doctorId(), request.startAt(), request.endAt(),
                AppointmentStatus.REQUESTED, null, Instant.now());
        Appointment saved = appointments.save(appointment);
        notifyUser(patient, NotificationType.APPOINTMENT, "Appointment requested", "Your appointment request was created");
        audit(patient, "APPOINTMENT_BOOKED", "Appointment", saved.getId(), "SUCCESS");
        return saved;
    }

    public List<Appointment> patientAppointments(String username) { return appointments.findByPatientUsernameOrderByStartAtDesc(username); }
    public List<Appointment> doctorAppointments(String doctorId) { return appointments.findByDoctorIdOrderByStartAtDesc(doctorId); }
    public List<Appointment> doctorAppointmentsForUser(String username) {
        String doctorId = doctors.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Doctor profile not found")).getId();
        return doctorAppointments(doctorId);
    }

    public Appointment updateAppointment(String id, AppointmentStatus status, String actor, boolean doctor) {
        Appointment appointment = appointments.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (!doctor && !appointment.getPatientUsername().equals(actor)) throw new IllegalArgumentException("Appointment does not belong to user");
        if (status == AppointmentStatus.CONFIRMED && !doctor) throw new IllegalArgumentException("Only a doctor can confirm an appointment");
        appointment.setStatus(status);
        Appointment saved = appointments.save(appointment);
        audit(actor, "APPOINTMENT_" + status, "Appointment", id, "SUCCESS");
        return saved;
    }

    public ClinicalRecord createClinicalRecord(String doctor, ClinicalRecordRequest request) {
        if (!request.patientConsent()) throw new IllegalArgumentException("Patient consent is required");
        ClinicalRecord record = new ClinicalRecord(null, request.patientUsername(), doctor, request.consultationId(), request.diagnosis(),
                request.notes(), request.attachmentUrls(), true, Instant.now(), Instant.now());
        ClinicalRecord saved = clinicalRecords.save(record);
        audit(doctor, "CLINICAL_RECORD_CREATED", "ClinicalRecord", saved.getId(), "SUCCESS");
        return saved;
    }

    public List<ClinicalRecord> patientRecords(String patient) { return clinicalRecords.findByPatientUsernameOrderByCreatedAtDesc(patient); }
    public List<ClinicalRecord> doctorRecords(String doctor) { return clinicalRecords.findByDoctorUsernameOrderByCreatedAtDesc(doctor); }

    public Prescription createPrescription(String doctor, PrescriptionRequest request) {
        Prescription prescription = new Prescription(null, request.patientUsername(), doctor, request.consultationId(),
                request.items().stream().map(i -> new PrescriptionItem(i.medicineName(), i.dosage(), i.frequency(), i.durationDays())).toList(),
                request.instructions(), PrescriptionStatus.ACTIVE, Instant.now());
        Prescription saved = prescriptions.save(prescription);
        notifyUser(request.patientUsername(), NotificationType.PRESCRIPTION, "New prescription", "A doctor issued a prescription for you");
        audit(doctor, "PRESCRIPTION_CREATED", "Prescription", saved.getId(), "SUCCESS");
        return saved;
    }

    public List<Prescription> patientPrescriptions(String patient) { return prescriptions.findByPatientUsernameOrderByIssuedAtDesc(patient); }
    public List<Prescription> doctorPrescriptions(String doctor) { return prescriptions.findByDoctorUsernameOrderByIssuedAtDesc(doctor); }

    public PharmacyInventory upsertInventory(InventoryRequest request, String actor) {
        PharmacyInventory item = inventory.findByPharmacyIdAndSku(request.pharmacyId(), request.sku())
                .orElseGet(() -> new PharmacyInventory(null, request.pharmacyId(), request.medicineName(), request.sku(), 0, request.unitPrice(), request.prescriptionRequired(), Instant.now()));
        item.setMedicineName(request.medicineName()); item.setQuantity(request.quantity()); item.setUnitPrice(request.unitPrice());
        item.setPrescriptionRequired(request.prescriptionRequired()); item.setUpdatedAt(Instant.now());
        PharmacyInventory saved = inventory.save(item);
        audit(actor, "INVENTORY_UPSERTED", "PharmacyInventory", saved.getId(), "SUCCESS");
        return saved;
    }

    public PharmacyInventory adjustInventory(String id, int quantity, String actor) {
        PharmacyInventory item = inventory.findById(id).orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));
        item.setQuantity(quantity); item.setUpdatedAt(Instant.now());
        PharmacyInventory saved = inventory.save(item);
        audit(actor, "INVENTORY_ADJUSTED", "PharmacyInventory", id, "SUCCESS");
        return saved;
    }

    public List<PharmacyInventory> pharmacyInventory(String pharmacyId) { return inventory.findByPharmacyIdOrderByMedicineNameAsc(pharmacyId); }

    public PaymentTransaction createPayment(String payer, PaymentRequest request) {
        PaymentTransaction payment = new PaymentTransaction(null, payer, request.referenceType(), request.referenceId(), request.amount(),
                request.currency().toUpperCase(), PaymentStatus.CREATED, null, Instant.now(), Instant.now());
        PaymentTransaction saved = payments.save(payment);
        notifyUser(payer, NotificationType.PAYMENT, "Payment created", "Your payment is ready to be processed");
        audit(payer, "PAYMENT_CREATED", "PaymentTransaction", saved.getId(), "SUCCESS");
        return saved;
    }

    public PaymentTransaction updatePayment(String id, PaymentStatus status, String providerReference, String actor) {
        PaymentTransaction payment = payments.findById(id).orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        payment.setStatus(status); payment.setProviderReference(providerReference); payment.setUpdatedAt(Instant.now());
        PaymentTransaction saved = payments.save(payment);
        audit(actor, "PAYMENT_" + status, "PaymentTransaction", id, "SUCCESS");
        return saved;
    }

    public List<PaymentTransaction> payments(String username) { return payments.findByPayerUsernameOrderByCreatedAtDesc(username); }

    public Notification notifyUser(String username, NotificationType type, String title, String message) {
        return notifications.save(new Notification(null, username, type, title, message, false, Instant.now()));
    }

    public List<Notification> notifications(String username) { return notifications.findByUsernameOrderByCreatedAtDesc(username); }

    public Notification markNotificationRead(String id, String username) {
        Notification notification = notifications.findById(id).orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getUsername().equals(username)) throw new IllegalArgumentException("Notification does not belong to user");
        notification.setRead(true);
        return notifications.save(notification);
    }

    public ConsentRecord grantConsent(String patient, ConsentRequest request) {
        ConsentRecord record = consents.findByPatientUsernameAndGrantedToAndPurpose(patient, request.grantedTo(), request.purpose())
                .orElse(new ConsentRecord(null, patient, request.grantedTo(), request.purpose(), true, Instant.now(), null));
        record.setActive(true); record.setRevokedAt(null);
        ConsentRecord saved = consents.save(record);
        audit(patient, "CONSENT_GRANTED", "ConsentRecord", saved.getId(), "SUCCESS");
        return saved;
    }

    public ConsentRecord revokeConsent(String id, String patient) {
        ConsentRecord record = consents.findById(id).orElseThrow(() -> new IllegalArgumentException("Consent not found"));
        if (!record.getPatientUsername().equals(patient)) throw new IllegalArgumentException("Consent does not belong to user");
        record.setActive(false); record.setRevokedAt(Instant.now());
        ConsentRecord saved = consents.save(record);
        audit(patient, "CONSENT_REVOKED", "ConsentRecord", id, "SUCCESS");
        return saved;
    }

    public List<ConsentRecord> activeConsents(String patient) { return consents.findByPatientUsernameAndActiveTrue(patient); }

    public WellnessCampaign createCampaign(CampaignRequest request, String actor) {
        if (request.endDate().isBefore(request.startDate())) throw new IllegalArgumentException("Campaign end date must not precede start date");
        WellnessCampaign campaign = new WellnessCampaign(null, request.sponsor(), request.title(), request.description(), request.budget(),
                request.startDate(), request.endDate(), CampaignStatus.DRAFT, Instant.now());
        WellnessCampaign saved = campaigns.save(campaign);
        audit(actor, "CAMPAIGN_CREATED", "WellnessCampaign", saved.getId(), "SUCCESS");
        return saved;
    }

    public List<WellnessCampaign> activeCampaigns() { return campaigns.findByStatusOrderByStartDateAsc(CampaignStatus.ACTIVE); }

    public void audit(String actor, String action, String entityType, String entityId, String outcome) {
        audits.save(new AuditEvent(null, actor, action, entityType, entityId, UUID.randomUUID().toString(), outcome, Instant.now()));
    }

    public long auditCount(String action) { return audits.countByAction(action); }
}
