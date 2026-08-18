package com.health.care.rest.controller;

import com.health.care.dtos.*;
import com.health.care.entities.*;
import com.health.care.services.PlatformOperations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/platform")
@Tag(name = "Platform Operations", description = "Appointments, clinical care, pharmacy, payments, notifications, consent, audit, and sponsored campaigns")
@SecurityRequirement(name = "bearerAuth")
public class PlatformController {
    private final PlatformOperations service;

    public PlatformController(PlatformOperations service) {
        this.service = service;
    }
    private String user(Authentication auth) { return auth.getName(); }

    @PostMapping("/doctor-verifications")
    @Operation(summary = "Submit doctor verification")
    public ResponseEntity<HealthApiResponse<DoctorVerification>> submitVerification(@Valid @RequestBody VerificationRequest request, Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(service.submitVerification(request, user(auth))));
    }

    @GetMapping("/doctor-verifications/pending")
    @Operation(summary = "List pending doctor verifications")
    public ResponseEntity<HealthApiResponse<List<DoctorVerification>>> pendingVerifications() { return ResponseEntity.ok(HealthApiResponse.success(service.pendingVerifications())); }

    @PatchMapping("/doctor-verifications/{id}")
    @Operation(summary = "Approve or reject doctor verification")
    public ResponseEntity<HealthApiResponse<DoctorVerification>> decideVerification(@PathVariable String id, @Valid @RequestBody VerificationDecision request, Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(service.decideVerification(id, request, user(auth))));
    }

    @PostMapping("/appointments")
    @Operation(summary = "Book an appointment")
    public ResponseEntity<HealthApiResponse<Appointment>> bookAppointment(@Valid @RequestBody AppointmentRequest request, Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.bookAppointment(user(auth), request))); }

    @GetMapping("/appointments/mine")
    @Operation(summary = "List my appointments")
    public ResponseEntity<HealthApiResponse<List<Appointment>>> myAppointments(Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.patientAppointments(user(auth)))); }

    @GetMapping("/appointments/doctor/mine")
    @Operation(summary = "List the authenticated doctor's appointments")
    public ResponseEntity<HealthApiResponse<List<Appointment>>> myDoctorAppointments(Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.doctorAppointmentsForUser(user(auth)))); }

    @GetMapping("/appointments/doctor/{doctorId}")
    @Operation(summary = "List a doctor's appointments for administration")
    public ResponseEntity<HealthApiResponse<List<Appointment>>> doctorAppointments(@PathVariable String doctorId) { return ResponseEntity.ok(HealthApiResponse.success(service.doctorAppointments(doctorId))); }

    @PatchMapping("/appointments/{id}/status")
    @Operation(summary = "Update appointment status")
    public ResponseEntity<HealthApiResponse<Appointment>> updateAppointment(@PathVariable String id, @Valid @RequestBody AppointmentStatusRequest request, Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(service.updateAppointment(id, request.status(), user(auth))));
    }

    @PostMapping("/clinical-records")
    @Operation(summary = "Create a consented clinical record")
    public ResponseEntity<HealthApiResponse<ClinicalRecord>> createClinicalRecord(@Valid @RequestBody ClinicalRecordRequest request, Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.createClinicalRecord(user(auth), request))); }

    @GetMapping("/clinical-records/mine")
    @Operation(summary = "List my clinical records")
    public ResponseEntity<HealthApiResponse<List<ClinicalRecord>>> myClinicalRecords(Authentication auth) {
        boolean doctor = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
        return ResponseEntity.ok(HealthApiResponse.success(doctor ? service.doctorRecords(user(auth)) : service.patientRecords(user(auth))));
    }

    @PostMapping("/prescriptions")
    @Operation(summary = "Issue a prescription")
    public ResponseEntity<HealthApiResponse<Prescription>> createPrescription(@Valid @RequestBody PrescriptionRequest request, Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.createPrescription(user(auth), request))); }

    @GetMapping("/prescriptions/mine")
    @Operation(summary = "List my prescriptions")
    public ResponseEntity<HealthApiResponse<List<Prescription>>> myPrescriptions(Authentication auth) {
        boolean doctor = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
        return ResponseEntity.ok(HealthApiResponse.success(doctor ? service.doctorPrescriptions(user(auth)) : service.patientPrescriptions(user(auth))));
    }

    @PostMapping("/inventory")
    @Operation(summary = "Create or replace pharmacy inventory")
    public ResponseEntity<HealthApiResponse<PharmacyInventory>> upsertInventory(@Valid @RequestBody InventoryRequest request, Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.upsertInventory(request, user(auth)))); }

    @PatchMapping("/inventory/{id}")
    @Operation(summary = "Adjust inventory quantity")
    public ResponseEntity<HealthApiResponse<PharmacyInventory>> adjustInventory(@PathVariable String id, @Valid @RequestBody InventoryAdjustment request, Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.adjustInventory(id, request.quantity(), user(auth)))); }

    @GetMapping("/inventory/{pharmacyId}")
    @Operation(summary = "List pharmacy inventory")
    public ResponseEntity<HealthApiResponse<List<PharmacyInventory>>> inventory(@PathVariable String pharmacyId) { return ResponseEntity.ok(HealthApiResponse.success(service.pharmacyInventory(pharmacyId))); }

    @PostMapping("/payments")
    @Operation(summary = "Create a payment transaction")
    public ResponseEntity<HealthApiResponse<PaymentTransaction>> createPayment(@Valid @RequestBody PaymentRequest request, Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.createPayment(user(auth), request))); }

    @GetMapping("/payments/mine")
    @Operation(summary = "List my payments")
    public ResponseEntity<HealthApiResponse<List<PaymentTransaction>>> myPayments(Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.payments(user(auth)))); }

    @PatchMapping("/payments/{id}")
    @Operation(summary = "Update payment status")
    public ResponseEntity<HealthApiResponse<PaymentTransaction>> updatePayment(@PathVariable String id, @Valid @RequestBody PaymentStatusRequest request, Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.updatePayment(id, request.status(), request.providerReference(), user(auth)))); }

    @GetMapping("/notifications")
    @Operation(summary = "List my notifications")
    public ResponseEntity<HealthApiResponse<List<Notification>>> notifications(Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.notifications(user(auth)))); }

    @PatchMapping("/notifications/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<HealthApiResponse<Notification>> markNotificationRead(@PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(service.markNotificationRead(id, user(auth))));
    }

    @PostMapping("/consents")
    @Operation(summary = "Grant data access consent")
    public ResponseEntity<HealthApiResponse<ConsentRecord>> grantConsent(@Valid @RequestBody ConsentRequest request, Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.grantConsent(user(auth), request))); }

    @PatchMapping("/consents/{id}/revoke")
    @Operation(summary = "Revoke data access consent")
    public ResponseEntity<HealthApiResponse<ConsentRecord>> revokeConsent(@PathVariable String id, Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.revokeConsent(id, user(auth)))); }

    @GetMapping("/consents")
    @Operation(summary = "List active consents")
    public ResponseEntity<HealthApiResponse<List<ConsentRecord>>> consents(Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.activeConsents(user(auth)))); }

    @PostMapping("/campaigns")
    @Operation(summary = "Create a sponsored wellness campaign")
    public ResponseEntity<HealthApiResponse<WellnessCampaign>> createCampaign(@Valid @RequestBody CampaignRequest request, Authentication auth) { return ResponseEntity.ok(HealthApiResponse.success(service.createCampaign(request, user(auth)))); }

    @GetMapping("/campaigns/active")
    @Operation(summary = "List active wellness campaigns")
    public ResponseEntity<HealthApiResponse<List<WellnessCampaign>>> activeCampaigns() { return ResponseEntity.ok(HealthApiResponse.success(service.activeCampaigns())); }

    @GetMapping("/audit/count/{action}")
    @Operation(summary = "Count audit events by action")
    public ResponseEntity<HealthApiResponse<Long>> auditCount(@PathVariable String action) { return ResponseEntity.ok(HealthApiResponse.success(service.auditCount(action))); }
}
