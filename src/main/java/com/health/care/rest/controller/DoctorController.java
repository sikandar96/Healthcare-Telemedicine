package com.health.care.rest.controller;

import com.health.care.dtos.DoctorRequest;
import com.health.care.dtos.HealthApiResponse;
import com.health.care.entities.DoctorProfile;
import com.health.care.services.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping("/register")
    public ResponseEntity<HealthApiResponse<DoctorProfile>> registerDoctor(@Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(doctorService.registerDoctor(request)));
    }

    @GetMapping("/available")
    public ResponseEntity<HealthApiResponse<List<DoctorProfile>>> doctors() {
        return ResponseEntity.ok(HealthApiResponse.success(doctorService.availableDoctors()));
    }
}
