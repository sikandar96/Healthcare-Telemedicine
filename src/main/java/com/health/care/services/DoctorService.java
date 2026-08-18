package com.health.care.services;

import com.health.care.dtos.DoctorRequest;
import com.health.care.entities.DoctorProfile;
import com.health.care.repositories.ConsultationRepository;
import com.health.care.repositories.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class DoctorService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorService.class);
    private final DoctorRepository doctors;

    public DoctorService(DoctorRepository doctors, ConsultationRepository consultations) {
        this.doctors = doctors;
    }

    public DoctorProfile registerDoctor(DoctorRequest request) {
        logger.info("Registering doctor profile for username '{}'", request.username());
        DoctorProfile doctor = new DoctorProfile(null, request.username(), request.name(), request.specialization(),
                request.licenseNumber(), true, true, money(request.consultationFee()), request.bio());
        DoctorProfile saved = doctors.save(doctor);
        logger.info("Doctor profile '{}' registered for username '{}'", saved.getId(), saved.getUsername());
        return saved;
    }

    public List<DoctorProfile> availableDoctors() {
        List<DoctorProfile> available = doctors.findByCertifiedTrueAndAvailableTrue();
        logger.debug("Found {} available certified doctors", available.size());
        return available;
    }


    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
