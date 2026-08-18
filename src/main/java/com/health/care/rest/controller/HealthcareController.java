package com.health.care.rest.controller;

import com.health.care.dtos.*;
import com.health.care.entities.*;
import com.health.care.services.ConsultationService;
import com.health.care.services.DoctorService;
import com.health.care.services.HealthcareService;
import com.health.care.services.PharmacyService;
import com.health.care.services.PreventiveReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/healthcare")
@Tag(name = "Healthcare Platform", description = "Consultations, pharmacy delivery, preventive health, awareness programs, and revenue")
@SecurityRequirement(name = "bearerAuth")
public class HealthcareController {
    private final DoctorService doctors;
    private final ConsultationService consultations;
    private final PharmacyService pharmacies;
    private final HealthcareService healthcare;
    private final PreventiveReminderService reminders;

    public HealthcareController(DoctorService doctors, ConsultationService consultations, PharmacyService pharmacies,
                                HealthcareService healthcare, PreventiveReminderService reminders) {
        this.doctors = doctors;
        this.consultations = consultations;
        this.pharmacies = pharmacies;
        this.healthcare = healthcare;
        this.reminders = reminders;
    }

    @PostMapping("/doctors")
    @Operation(summary = "Register a doctor")
    public ResponseEntity<HealthApiResponse<DoctorProfile>> registerDoctor(@Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(doctors.registerDoctor(request)));
    }

    @GetMapping("/doctors")
    @Operation(summary = "List available doctors")
    public ResponseEntity<HealthApiResponse<List<DoctorProfile>>> doctors() {
        return ResponseEntity.ok(HealthApiResponse.success(doctors.availableDoctors()));
    }

    @PostMapping("/consultations")
    @Operation(summary = "Book a consultation")
    public ResponseEntity<HealthApiResponse<Consultation>> bookConsultation(Authentication auth,
                                                                              @Valid @RequestBody ConsultationRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(consultations.bookConsultation(auth.getName(), request)));
    }

    @GetMapping("/consultations")
    @Operation(summary = "List the authenticated user's consultations")
    public ResponseEntity<HealthApiResponse<List<Consultation>>> consultations(Authentication auth) {
        boolean doctor = auth.getAuthorities().stream().anyMatch(a -> "ROLE_DOCTOR".equals(a.getAuthority()));
        return ResponseEntity.ok(HealthApiResponse.success(consultations.consultationsFor(auth.getName(), doctor)));
    }

    @PatchMapping("/consultations/{id}/status")
    @Operation(summary = "Update consultation status")
    public ResponseEntity<HealthApiResponse<Consultation>> updateConsultation(Authentication auth, @PathVariable String id,
                                                                                @Valid @RequestBody ConsultationStatusRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(consultations.updateConsultation(id, auth.getName(), request.status())));
    }

    @PostMapping("/pharmacies")
    @Operation(summary = "Register a pharmacy")
    public ResponseEntity<HealthApiResponse<Pharmacy>> addPharmacy(@Valid @RequestBody PharmacyRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(pharmacies.addPharmacy(request)));
    }

    @GetMapping("/pharmacies")
    @Operation(summary = "List available pharmacies")
    public ResponseEntity<HealthApiResponse<List<Pharmacy>>> pharmacies() {
        return ResponseEntity.ok(HealthApiResponse.success(pharmacies.availablePharmacies()));
    }

    @PostMapping("/medicine-orders")
    @Operation(summary = "Place a medicine order")
    public ResponseEntity<HealthApiResponse<MedicineOrder>> orderMedicine(Authentication auth,
                                                                            @Valid @RequestBody MedicineOrderRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(pharmacies.placeMedicineOrder(auth.getName(), request)));
    }

    @GetMapping("/medicine-orders")
    @Operation(summary = "List the authenticated patient's medicine orders")
    public ResponseEntity<HealthApiResponse<List<MedicineOrder>>> orders(Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(pharmacies.patientOrders(auth.getName())));
    }

    @PostMapping("/health-programs")
    @Operation(summary = "Publish a health-awareness program")
    public ResponseEntity<HealthApiResponse<HealthProgram>> publishProgram(@Valid @RequestBody HealthProgramRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(healthcare.publishProgram(request)));
    }

    @GetMapping("/health-programs")
    @Operation(summary = "List published health programs")
    @SecurityRequirements
    public ResponseEntity<HealthApiResponse<List<HealthProgram>>> programs() {
        return ResponseEntity.ok(HealthApiResponse.success(healthcare.publishedPrograms()));
    }

    @PostMapping("/reminders")
    @Operation(summary = "Create a preventive reminder")
    public ResponseEntity<HealthApiResponse<PreventiveReminder>> createReminder(Authentication auth,
                                                                                  @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(reminders.createReminder(auth.getName(), request)));
    }

    @GetMapping("/reminders")
    @Operation(summary = "List the authenticated patient's reminders")
    public ResponseEntity<HealthApiResponse<List<PreventiveReminder>>> reminders(Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(reminders.reminders(auth.getName())));
    }

    @PatchMapping("/reminders/{id}/complete")
    @Operation(summary = "Complete a preventive reminder")
    public ResponseEntity<HealthApiResponse<PreventiveReminder>> completeReminder(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(HealthApiResponse.success(reminders.completeReminder(id, auth.getName())));
    }

    @GetMapping("/revenue/summary")
    @Operation(summary = "Get the platform revenue summary")
    public ResponseEntity<HealthApiResponse<RevenueSummary>> revenueSummary() {
        return ResponseEntity.ok(HealthApiResponse.success(healthcare.revenueSummary()));
    }
}
