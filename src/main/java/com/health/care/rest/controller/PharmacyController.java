package com.health.care.rest.controller;

import com.health.care.dtos.HealthApiResponse;
import com.health.care.dtos.MedicineOrderRequest;
import com.health.care.dtos.PharmacyRequest;
import com.health.care.entities.MedicineOrder;
import com.health.care.entities.Pharmacy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.health.care.services.PharmacyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacies")
@Tag(name = "Pharmacies and Medicine Delivery", description = "Local pharmacy partnerships and medicine orders")
@SecurityRequirement(name = "bearerAuth")
public class PharmacyController {

    private final PharmacyService service;

    public PharmacyController(PharmacyService service) {
        this.service = service;
    }

    @Operation(summary = "Add a pharmacy partner", description = "Creates a verified pharmacy profile. Requires HEALTH_MANAGER or ADMIN authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pharmacy added"),
            @ApiResponse(responseCode = "400", description = "Invalid pharmacy details"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient role")
    })
    @PostMapping("/add")
    public ResponseEntity<HealthApiResponse<Pharmacy>> addPharmacy(@Valid @RequestBody PharmacyRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.addPharmacy(request)));
    }

    @Operation(summary = "List available pharmacies", description = "Returns active and verified local pharmacies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Available pharmacies returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/available")
    public ResponseEntity<HealthApiResponse<List<Pharmacy>>> pharmacies() {
        return ResponseEntity.ok(HealthApiResponse.success(service.availablePharmacies()));
    }

    @Operation(summary = "Place a medicine order", description = "Creates a medicine delivery order with the selected local pharmacy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicine order placed"),
            @ApiResponse(responseCode = "400", description = "Invalid order or unavailable pharmacy"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Patient role required")
    })
    @PostMapping("/medicine-orders")
    public ResponseEntity<HealthApiResponse<MedicineOrder>> orderMedicine(Authentication auth,
                                                                          @Valid @RequestBody MedicineOrderRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.placeMedicineOrder(auth.getName(), request)));
    }

    @Operation(summary = "List my medicine orders", description = "Returns medicine orders created by the authenticated patient.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicine orders returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Patient role required")
    })
    @GetMapping("/medicine-orders")
    public ResponseEntity<HealthApiResponse<List<MedicineOrder>>> orders(Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(service.patientOrders(auth.getName())));
    }
}
