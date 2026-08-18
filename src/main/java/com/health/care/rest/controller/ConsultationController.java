package com.health.care.rest.controller;

import com.health.care.dtos.ConsultationRequest;
import com.health.care.dtos.ConsultationStatusRequest;
import com.health.care.dtos.HealthApiResponse;
import com.health.care.entities.Consultation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.health.care.services.ConsultationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
@Tag(name = "Consultations", description = "Video and audio doctor consultation lifecycle")
@SecurityRequirement(name = "bearerAuth")
public class ConsultationController {

    private final ConsultationService service;

    public ConsultationController(ConsultationService service) {
        this.service = service;
    }

    @Operation(summary = "Book a consultation", description = "Books a video or audio consultation for the authenticated patient.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consultation booked"),
            @ApiResponse(responseCode = "400", description = "Invalid schedule or consultation request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Patient role required")
    })
    @PostMapping("/book")
    public ResponseEntity<HealthApiResponse<Consultation>> bookConsultation(Authentication auth,
                                                                            @Valid @RequestBody ConsultationRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.bookConsultation(auth.getName(), request)));
    }

    @Operation(summary = "List my consultations", description = "Returns consultations associated with the authenticated patient or doctor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consultations returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Patient or doctor role required")
    })
    @GetMapping("/my")
    public ResponseEntity<HealthApiResponse<List<Consultation>>> consultations(Authentication auth) {
        boolean doctor = auth.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_DOCTOR".equals(authority.getAuthority()));
        return ResponseEntity.ok(HealthApiResponse.success(service.consultationsFor(auth.getName(), doctor)));
    }

    @Operation(summary = "Update consultation status", description = "Updates a consultation status after validating that the authenticated user participates in it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consultation status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status or participant operation"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Patient or doctor role required"),
            @ApiResponse(responseCode = "404", description = "Consultation not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<HealthApiResponse<Consultation>> updateConsultation(Authentication auth, @Parameter(description = "Consultation identifier", required = true) @PathVariable String id,
                                                                              @Valid @RequestBody ConsultationStatusRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.updateConsultation(id, auth.getName(), request.status())));
    }
}
