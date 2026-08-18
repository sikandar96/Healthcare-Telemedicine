package com.health.care.healthcare;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

interface DoctorRepository extends MongoRepository<DoctorProfile, String> {
    List<DoctorProfile> findByCertifiedTrueAndAvailableTrue();
}

interface ConsultationRepository extends MongoRepository<Consultation, String> {
    List<Consultation> findByPatientUsernameOrderByScheduledAtDesc(String username);
    List<Consultation> findByDoctorIdOrderByScheduledAtDesc(String doctorId);
}

interface PharmacyRepository extends MongoRepository<Pharmacy, String> {
    List<Pharmacy> findByActiveTrueAndVerifiedTrue();
}

interface MedicineOrderRepository extends MongoRepository<MedicineOrder, String> {
    List<MedicineOrder> findByPatientUsernameOrderByCreatedAtDesc(String username);
}

interface HealthProgramRepository extends MongoRepository<HealthProgram, String> {
    List<HealthProgram> findByPublishedTrueOrderByPublishedAtDesc();
}

interface PreventiveReminderRepository extends MongoRepository<PreventiveReminder, String> {
    List<PreventiveReminder> findByUsernameOrderByDueDateAsc(String username);
    List<PreventiveReminder> findByDueDateLessThanEqualAndCompletedFalseAndNotifiedFalse(java.time.LocalDate date);
}
