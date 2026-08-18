package com.health.care.platform;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

interface DoctorVerificationRepository extends MongoRepository<DoctorVerification, String> {
    Optional<DoctorVerification> findTopByUsernameOrderBySubmittedAtDesc(String username);
    List<DoctorVerification> findByStatusOrderBySubmittedAtAsc(VerificationStatus status);
}

interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByPatientUsernameOrderByStartAtDesc(String username);
    List<Appointment> findByDoctorIdOrderByStartAtDesc(String doctorId);
    boolean existsByDoctorIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(String doctorId, List<AppointmentStatus> statuses, LocalDateTime endAt, LocalDateTime startAt);
}

interface ClinicalRecordRepository extends MongoRepository<ClinicalRecord, String> {
    List<ClinicalRecord> findByPatientUsernameOrderByCreatedAtDesc(String username);
    List<ClinicalRecord> findByDoctorUsernameOrderByCreatedAtDesc(String username);
}

interface PrescriptionRepository extends MongoRepository<Prescription, String> {
    List<Prescription> findByPatientUsernameOrderByIssuedAtDesc(String username);
    List<Prescription> findByDoctorUsernameOrderByIssuedAtDesc(String username);
}

interface PharmacyInventoryRepository extends MongoRepository<PharmacyInventory, String> {
    List<PharmacyInventory> findByPharmacyIdOrderByMedicineNameAsc(String pharmacyId);
    Optional<PharmacyInventory> findByPharmacyIdAndSku(String pharmacyId, String sku);
}

interface PaymentTransactionRepository extends MongoRepository<PaymentTransaction, String> {
    List<PaymentTransaction> findByPayerUsernameOrderByCreatedAtDesc(String username);
    List<PaymentTransaction> findByStatus(PaymentStatus status);
    List<PaymentTransaction> findByReferenceTypeAndReferenceId(String type, String referenceId);
}

interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUsernameOrderByCreatedAtDesc(String username);
}

interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
    List<AuditEvent> findByActorOrderByOccurredAtDesc(String actor);
    long countByAction(String action);
}

interface ConsentRecordRepository extends MongoRepository<ConsentRecord, String> {
    List<ConsentRecord> findByPatientUsernameAndActiveTrue(String username);
    Optional<ConsentRecord> findByPatientUsernameAndGrantedToAndPurpose(String patient, String grantedTo, String purpose);
}

interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByTokenHashAndUsedFalse(String tokenHash);
}

interface WellnessCampaignRepository extends MongoRepository<WellnessCampaign, String> {
    List<WellnessCampaign> findByStatusOrderByStartDateAsc(CampaignStatus status);
}
