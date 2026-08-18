package com.health.care.rest.controller;

import com.health.care.dtos.DoctorRequest;
import com.health.care.dtos.HealthApiResponse;
import com.health.care.entities.DoctorProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.health.care.services.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@Tag(name = "Doctors", description = "Certified doctor registration and availability")
@SecurityRequirement(name = "bearerAuth")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @Operation(summary = "Register a certified doctor", description = "Creates a doctor profile. Requires HEALTH_MANAGER or ADMIN authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor registered"),
            @ApiResponse(responseCode = "400", description = "Invalid doctor profile"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient role")
    })
    @PostMapping("/register")
    public ResponseEntity<HealthApiResponse<DoctorProfile>> registerDoctor(@Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(doctorService.registerDoctor(request)));
    }

    @Operation(summary = "List available certified doctors", description = "Returns doctors currently marked certified and available.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Available doctors returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/available")
    public ResponseEntity<HealthApiResponse<List<DoctorProfile>>> doctors() {
        return ResponseEntity.ok(HealthApiResponse.success(doctorService.availableDoctors()));
    }
}
