package com.health.care.rest.controller;

import com.health.care.dtos.ConsultationRequest;
import com.health.care.dtos.ConsultationStatusRequest;
import com.health.care.dtos.HealthApiResponse;
import com.health.care.entities.Consultation;
import com.health.care.services.ConsultationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService service;

    public ConsultationController(ConsultationService service) {
        this.service = service;
    }

    @PostMapping("/book")
    public ResponseEntity<HealthApiResponse<Consultation>> bookConsultation(Authentication auth,
                                                                            @Valid @RequestBody ConsultationRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.bookConsultation(auth.getName(), request)));
    }

    @GetMapping("/my")
    public ResponseEntity<HealthApiResponse<List<Consultation>>> consultations(Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(service.patientConsultations(auth.getName())));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<HealthApiResponse<Consultation>> updateConsultation(Authentication auth, @PathVariable String id,
                                                                              @Valid @RequestBody ConsultationStatusRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.updateConsultation(id, auth.getName(), request.status())));
    }
}
