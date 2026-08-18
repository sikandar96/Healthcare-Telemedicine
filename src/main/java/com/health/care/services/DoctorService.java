package com.health.care.services;

import com.health.care.dtos.DoctorRequest;
import com.health.care.entities.DoctorProfile;
import com.health.care.repositories.ConsultationRepository;
import com.health.care.repositories.DoctorRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctors;

    public DoctorService(DoctorRepository doctors, ConsultationRepository consultations) {
        this.doctors = doctors;
    }

    public DoctorProfile registerDoctor(DoctorRequest request) {
        DoctorProfile doctor = new DoctorProfile(null, request.username(), request.name(), request.specialization(),
                request.licenseNumber(), true, true, money(request.consultationFee()), request.bio());
        return doctors.save(doctor);
    }

    public List<DoctorProfile> availableDoctors() {
        return doctors.findByCertifiedTrueAndAvailableTrue();
    }


    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
