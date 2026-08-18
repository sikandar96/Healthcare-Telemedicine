package com.health.care.rest.controller;

import com.health.care.dtos.*;
import com.health.care.entities.*;
import com.health.care.services.HealthcareService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/healthcare")
public class HealthcareController {
    private final HealthcareService service;

    public HealthcareController(HealthcareService service) {
        this.service = service;
    }


    @PostMapping("/health-programs")
    public ResponseEntity<HealthApiResponse<HealthProgram>> publishProgram(@Valid @RequestBody HealthProgramRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.publishProgram(request)));
    }

    @GetMapping("/health-programs")
    public ResponseEntity<HealthApiResponse<List<HealthProgram>>> programs() {
        return ResponseEntity.ok(HealthApiResponse.success(service.publishedPrograms()));
    }

    @GetMapping("/revenue/summary")
    public ResponseEntity<HealthApiResponse<RevenueSummary>> revenueSummary() {
        return ResponseEntity.ok(HealthApiResponse.success(service.revenueSummary()));
    }
}
