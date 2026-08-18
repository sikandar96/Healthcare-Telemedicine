package com.health.care.services;

import com.health.care.dtos.*;
import com.health.care.entities.*;
import com.health.care.repositories.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
public class HealthcareService {

    private final ConsultationRepository consultations;
    private final MedicineOrderRepository medicineOrders;
    private final HealthProgramRepository programs;

    public HealthcareService(ConsultationRepository consultations,
                             MedicineOrderRepository medicineOrders,
                             HealthProgramRepository programs) {
        this.consultations = consultations;
        this.medicineOrders = medicineOrders;
        this.programs = programs;
    }


    public HealthProgram publishProgram(HealthProgramRequest request) {
        return programs.save(new HealthProgram(null, request.title(), request.category(), request.content(),
                request.sponsorName(), request.sponsored(), money(request.sponsorshipFee()), true, Instant.now()));
    }

    public List<HealthProgram> publishedPrograms() {
        return programs.findByPublishedTrueOrderByPublishedAtDesc();
    }


    public RevenueSummary revenueSummary() {
        BigDecimal consultationGross = consultations.findAll().stream().map(Consultation::getFee).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal consultationCommission = consultations.findAll().stream().map(Consultation::getPlatformCommission).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pharmacyGross = medicineOrders.findAll().stream().map(MedicineOrder::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pharmacyCommission = medicineOrders.findAll().stream().map(MedicineOrder::getPharmacyCommission).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sponsored = programs.findAll().stream().filter(HealthProgram::isSponsored)
                .map(HealthProgram::getSponsorshipFee).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RevenueSummary(money(consultationGross), money(consultationCommission), money(pharmacyGross),
                money(pharmacyCommission), sponsored, money(consultationCommission.add(pharmacyCommission).add(sponsored)));
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
