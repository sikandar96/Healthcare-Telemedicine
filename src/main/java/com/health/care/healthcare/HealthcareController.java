package com.health.care.healthcare;

import com.health.care.dtos.HealthApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/healthcare")
public class HealthcareController {
    private final HealthcareService service;

    public HealthcareController(HealthcareService service) {
        this.service = service;
    }

    @PostMapping("/doctors")
    public ResponseEntity<HealthApiResponse<DoctorProfile>> registerDoctor(@Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.registerDoctor(request)));
    }

    @GetMapping("/doctors")
    public ResponseEntity<HealthApiResponse<List<DoctorProfile>>> doctors() {
        return ResponseEntity.ok(HealthApiResponse.success(service.availableDoctors()));
    }

    @PostMapping("/consultations")
    public ResponseEntity<HealthApiResponse<Consultation>> bookConsultation(Authentication auth,
                                                                             @Valid @RequestBody ConsultationRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.bookConsultation(auth.getName(), request)));
    }

    @GetMapping("/consultations")
    public ResponseEntity<HealthApiResponse<List<Consultation>>> consultations(Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(service.patientConsultations(auth.getName())));
    }

    @PatchMapping("/consultations/{id}/status")
    public ResponseEntity<HealthApiResponse<Consultation>> updateConsultation(Authentication auth, @PathVariable String id,
                                                                                @Valid @RequestBody ConsultationStatusRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.updateConsultation(id, auth.getName(), request.status())));
    }

    @PostMapping("/pharmacies")
    public ResponseEntity<HealthApiResponse<Pharmacy>> addPharmacy(@Valid @RequestBody PharmacyRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.addPharmacy(request)));
    }

    @GetMapping("/pharmacies")
    public ResponseEntity<HealthApiResponse<List<Pharmacy>>> pharmacies() {
        return ResponseEntity.ok(HealthApiResponse.success(service.availablePharmacies()));
    }

    @PostMapping("/medicine-orders")
    public ResponseEntity<HealthApiResponse<MedicineOrder>> orderMedicine(Authentication auth,
                                                                            @Valid @RequestBody MedicineOrderRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.placeMedicineOrder(auth.getName(), request)));
    }

    @GetMapping("/medicine-orders")
    public ResponseEntity<HealthApiResponse<List<MedicineOrder>>> orders(Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(service.patientOrders(auth.getName())));
    }

    @PostMapping("/health-programs")
    public ResponseEntity<HealthApiResponse<HealthProgram>> publishProgram(@Valid @RequestBody HealthProgramRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.publishProgram(request)));
    }

    @GetMapping("/health-programs")
    public ResponseEntity<HealthApiResponse<List<HealthProgram>>> programs() {
        return ResponseEntity.ok(HealthApiResponse.success(service.publishedPrograms()));
    }

    @PostMapping("/reminders")
    public ResponseEntity<HealthApiResponse<PreventiveReminder>> createReminder(Authentication auth,
                                                                                  @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.createReminder(auth.getName(), request)));
    }

    @GetMapping("/reminders")
    public ResponseEntity<HealthApiResponse<List<PreventiveReminder>>> reminders(Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(service.reminders(auth.getName())));
    }

    @PatchMapping("/reminders/{id}/complete")
    public ResponseEntity<HealthApiResponse<PreventiveReminder>> completeReminder(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(HealthApiResponse.success(service.completeReminder(id, auth.getName())));
    }

    @GetMapping("/revenue/summary")
    public ResponseEntity<HealthApiResponse<RevenueSummary>> revenueSummary() {
        return ResponseEntity.ok(HealthApiResponse.success(service.revenueSummary()));
    }
}
