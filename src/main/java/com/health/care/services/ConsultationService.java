package com.health.care.services;

import com.health.care.dtos.ConsultationRequest;
import com.health.care.entities.Consultation;
import com.health.care.entities.DoctorProfile;
import com.health.care.enums.ConsultationStatus;
import com.health.care.repositories.ConsultationRepository;
import com.health.care.repositories.DoctorRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ConsultationService {
    private static final BigDecimal CONSULTATION_COMMISSION_RATE = new BigDecimal("0.15");

    private final ConsultationRepository consultations;
    private final DoctorRepository doctors;

    public ConsultationService(ConsultationRepository consultations, DoctorRepository doctors) {
        this.consultations = consultations;
        this.doctors = doctors;
    }

    public Consultation bookConsultation(String patient, ConsultationRequest request) {
        DoctorProfile doctor = doctors.findById(request.doctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        if (!doctor.isCertified() || !doctor.isAvailable()) {
            throw new IllegalArgumentException("Doctor is not currently available for consultations");
        }
        BigDecimal fee = money(doctor.getConsultationFee());
        BigDecimal commission = money(fee.multiply(CONSULTATION_COMMISSION_RATE));
        Consultation consultation = new Consultation(null, patient, doctor.getId(), doctor.getName(), request.type(),
                ConsultationStatus.REQUESTED, request.scheduledAt(), null, null, fee, commission,
                money(fee.subtract(commission)), "https://call.healthcare.local/room/" + UUID.randomUUID(), Instant.now());
        return consultations.save(consultation);
    }

    public List<Consultation> patientConsultations(String patient) {
        return consultations.findByPatientUsernameOrderByScheduledAtDesc(patient);
    }

    public List<Consultation> consultationsFor(String username, boolean doctor) {
        if (!doctor) {
            return patientConsultations(username);
        }
        DoctorProfile profile = doctors.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Doctor profile not found"));
        return consultations.findByDoctorIdOrderByScheduledAtDesc(profile.getId());
    }

    public Consultation updateConsultation(String id, String caller, ConsultationStatus status) {
        Consultation consultation = consultations.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consultation not found"));
        if (!consultation.getPatientUsername().equals(caller)) {
            doctors.findById(consultation.getDoctorId())
                    .filter(d -> d.getUsername().equals(caller))
                    .orElseThrow(() -> new IllegalArgumentException("You are not a participant in this consultation"));
        }
        if (consultation.getStatus() == ConsultationStatus.COMPLETED || consultation.getStatus() == ConsultationStatus.CANCELLED) {
            throw new IllegalArgumentException("A closed consultation cannot be updated");
        }
        consultation.setStatus(status);
        if (status == ConsultationStatus.IN_PROGRESS) consultation.setStartedAt(Instant.now());
        if (status == ConsultationStatus.COMPLETED) consultation.setCompletedAt(Instant.now());
        return consultations.save(consultation);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
