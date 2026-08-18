package com.health.care.healthcare;

import com.health.care.dtos.HealthApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/healthcare")
@Tag(name = "Healthcare Platform", description = "Consultations, pharmacy delivery, preventive health, awareness programs, and revenue")
@SecurityRequirement(name = "bearerAuth")
public class HealthcareController {
    private final HealthcareService service;

    public HealthcareController(@Qualifier("healthcareFeatureService") HealthcareService service) {
        this.service = service;
    }

    @Operation(summary = "Register a doctor", description = "Creates a certified doctor profile. Requires HEALTH_MANAGER or ADMIN authority.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Doctor registered"), @ApiResponse(responseCode = "400", description = "Invalid request"), @ApiResponse(responseCode = "401", description = "Authentication required"), @ApiResponse(responseCode = "403", description = "Insufficient role")})
    @PostMapping("/doctors")
    public ResponseEntity<HealthApiResponse<DoctorProfile>> registerDoctor(@Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.registerDoctor(request)));
    }

    @Operation(summary = "List available doctors", description = "Returns certified doctors available for consultation.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Doctors returned"), @ApiResponse(responseCode = "401", description = "Authentication required")})
    @GetMapping("/doctors")
    public ResponseEntity<HealthApiResponse<List<DoctorProfile>>> doctors() {
        return ResponseEntity.ok(HealthApiResponse.success(service.availableDoctors()));
    }

    @Operation(summary = "Book a consultation", description = "Books a video or audio consultation for the authenticated patient.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Consultation booked"), @ApiResponse(responseCode = "400", description = "Invalid request"), @ApiResponse(responseCode = "401", description = "Authentication required"), @ApiResponse(responseCode = "403", description = "Patient role required")})
    @PostMapping("/consultations")
    public ResponseEntity<HealthApiResponse<Consultation>> bookConsultation(Authentication auth,
                                                                             @Valid @RequestBody ConsultationRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.bookConsultation(auth.getName(), request)));
    }

    @Operation(summary = "List my consultations", description = "Returns consultations for the authenticated patient or doctor.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Consultations returned"), @ApiResponse(responseCode = "401", description = "Authentication required"), @ApiResponse(responseCode = "403", description = "Insufficient role")})
    @GetMapping("/consultations")
    public ResponseEntity<HealthApiResponse<List<Consultation>>> consultations(Authentication auth) {
        boolean doctor = auth.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_DOCTOR".equals(authority.getAuthority()));
        return ResponseEntity.ok(HealthApiResponse.success(service.consultationsFor(auth.getName(), doctor)));
    }

    @Operation(summary = "Update consultation status", description = "Updates a consultation status after participant validation.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Status updated"), @ApiResponse(responseCode = "400", description = "Invalid status"), @ApiResponse(responseCode = "401", description = "Authentication required"), @ApiResponse(responseCode = "404", description = "Consultation not found")})
    @PatchMapping("/consultations/{id}/status")
    public ResponseEntity<HealthApiResponse<Consultation>> updateConsultation(Authentication auth, @PathVariable String id,
                                                                                @Valid @RequestBody ConsultationStatusRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.updateConsultation(id, auth.getName(), request.status())));
    }

    @Operation(summary = "Register a pharmacy", description = "Adds a local pharmacy partner. Requires HEALTH_MANAGER or ADMIN authority.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pharmacy registered"), @ApiResponse(responseCode = "400", description = "Invalid request"), @ApiResponse(responseCode = "403", description = "Insufficient role")})
    @PostMapping("/pharmacies")
    public ResponseEntity<HealthApiResponse<Pharmacy>> addPharmacy(@Valid @RequestBody PharmacyRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.addPharmacy(request)));
    }

    @Operation(summary = "List available pharmacies", description = "Returns active local pharmacy partners.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pharmacies returned"), @ApiResponse(responseCode = "401", description = "Authentication required")})
    @GetMapping("/pharmacies")
    public ResponseEntity<HealthApiResponse<List<Pharmacy>>> pharmacies() {
        return ResponseEntity.ok(HealthApiResponse.success(service.availablePharmacies()));
    }

    @Operation(summary = "Place a medicine order", description = "Places a local-pharmacy medicine delivery order for the authenticated patient.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Order placed"), @ApiResponse(responseCode = "400", description = "Invalid order"), @ApiResponse(responseCode = "403", description = "Patient role required")})
    @PostMapping("/medicine-orders")
    public ResponseEntity<HealthApiResponse<MedicineOrder>> orderMedicine(Authentication auth,
                                                                            @Valid @RequestBody MedicineOrderRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.placeMedicineOrder(auth.getName(), request)));
    }

    @Operation(summary = "List my medicine orders", description = "Returns medicine orders for the authenticated patient.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Orders returned"), @ApiResponse(responseCode = "401", description = "Authentication required"), @ApiResponse(responseCode = "403", description = "Patient role required")})
    @GetMapping("/medicine-orders")
    public ResponseEntity<HealthApiResponse<List<MedicineOrder>>> orders(Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(service.patientOrders(auth.getName())));
    }

    @Operation(summary = "Publish a health-awareness program", description = "Publishes preventive-care or vaccination awareness content. Requires HEALTH_MANAGER or ADMIN authority.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Program published"), @ApiResponse(responseCode = "400", description = "Invalid program"), @ApiResponse(responseCode = "403", description = "Insufficient role")})
    @PostMapping("/health-programs")
    public ResponseEntity<HealthApiResponse<HealthProgram>> publishProgram(@Valid @RequestBody HealthProgramRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.publishProgram(request)));
    }

    @Operation(summary = "List published health programs", description = "Returns public preventive-care and vaccination awareness programs.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Programs returned")})
    @SecurityRequirements
    @GetMapping("/health-programs")
    public ResponseEntity<HealthApiResponse<List<HealthProgram>>> programs() {
        return ResponseEntity.ok(HealthApiResponse.success(service.publishedPrograms()));
    }

    @Operation(summary = "Create a preventive reminder", description = "Creates a vaccination, screening, medication, or checkup reminder for the authenticated patient.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Reminder created"), @ApiResponse(responseCode = "400", description = "Invalid reminder"), @ApiResponse(responseCode = "403", description = "Patient role required")})
    @PostMapping("/reminders")
    public ResponseEntity<HealthApiResponse<PreventiveReminder>> createReminder(Authentication auth,
                                                                                  @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.createReminder(auth.getName(), request)));
    }

    @Operation(summary = "List my reminders", description = "Returns preventive reminders for the authenticated patient.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Reminders returned"), @ApiResponse(responseCode = "403", description = "Patient role required")})
    @GetMapping("/reminders")
    public ResponseEntity<HealthApiResponse<List<PreventiveReminder>>> reminders(Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(service.reminders(auth.getName())));
    }

    @Operation(summary = "Complete a preventive reminder", description = "Marks a patient-owned preventive reminder as complete.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Reminder completed"), @ApiResponse(responseCode = "404", description = "Reminder not found")})
    @PatchMapping("/reminders/{id}/complete")
    public ResponseEntity<HealthApiResponse<PreventiveReminder>> completeReminder(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(HealthApiResponse.success(service.completeReminder(id, auth.getName())));
    }

    @Operation(summary = "Get revenue summary", description = "Returns consultation commissions, pharmacy commissions, sponsorship revenue, and total platform revenue. Requires HEALTH_MANAGER or ADMIN authority.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Revenue summary returned"), @ApiResponse(responseCode = "401", description = "Authentication required"), @ApiResponse(responseCode = "403", description = "Insufficient role")})
    @GetMapping("/revenue/summary")
    public ResponseEntity<HealthApiResponse<RevenueSummary>> revenueSummary() {
        return ResponseEntity.ok(HealthApiResponse.success(service.revenueSummary()));
    }
}
