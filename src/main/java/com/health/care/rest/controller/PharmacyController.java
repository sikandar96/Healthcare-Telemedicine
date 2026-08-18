package com.health.care.rest.controller;

import com.health.care.dtos.HealthApiResponse;
import com.health.care.dtos.MedicineOrderRequest;
import com.health.care.dtos.PharmacyRequest;
import com.health.care.entities.MedicineOrder;
import com.health.care.entities.Pharmacy;
import com.health.care.services.PharmacyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacies")
public class PharmacyController {

    private final PharmacyService service;

    public PharmacyController(PharmacyService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public ResponseEntity<HealthApiResponse<Pharmacy>> addPharmacy(@Valid @RequestBody PharmacyRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.addPharmacy(request)));
    }

    @GetMapping("/available")
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
}
