package com.health.care.healthcare;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("healthcareDoctorRepository")
interface DoctorRepository extends MongoRepository<DoctorProfile, String> {
    List<DoctorProfile> findByCertifiedTrueAndAvailableTrue();
    Optional<DoctorProfile> findByUsername(String username);
}

@Repository("healthcareConsultationRepository")
interface ConsultationRepository extends MongoRepository<Consultation, String> {
    List<Consultation> findByPatientUsernameOrderByScheduledAtDesc(String username);
    List<Consultation> findByDoctorIdOrderByScheduledAtDesc(String doctorId);
}

@Repository("healthcarePharmacyRepository")
interface PharmacyRepository extends MongoRepository<Pharmacy, String> {
    List<Pharmacy> findByActiveTrueAndVerifiedTrue();
}

@Repository("healthcareMedicineOrderRepository")
interface MedicineOrderRepository extends MongoRepository<MedicineOrder, String> {
    List<MedicineOrder> findByPatientUsernameOrderByCreatedAtDesc(String username);
}

@Repository("healthcareHealthProgramRepository")
interface HealthProgramRepository extends MongoRepository<HealthProgram, String> {
    List<HealthProgram> findByPublishedTrueOrderByPublishedAtDesc();
}

@Repository("healthcarePreventiveReminderRepository")
interface PreventiveReminderRepository extends MongoRepository<PreventiveReminder, String> {
    List<PreventiveReminder> findByUsernameOrderByDueDateAsc(String username);
    List<PreventiveReminder> findByDueDateLessThanEqualAndCompletedFalseAndNotifiedFalse(java.time.LocalDate date);
}
