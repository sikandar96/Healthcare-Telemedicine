package com.health.care.rest.controller;

import com.health.care.dtos.HealthApiResponse;
import com.health.care.dtos.ReminderRequest;
import com.health.care.entities.PreventiveReminder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.health.care.services.PreventiveReminderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@Tag(name = "Preventive Health Reminders", description = "Vaccination, screening, medication, and checkup reminders")
@SecurityRequirement(name = "bearerAuth")
public class PreventiveReminderController {

    private final PreventiveReminderService service;


    public PreventiveReminderController(PreventiveReminderService service) {
        this.service = service;
    }

    @Operation(summary = "Create a preventive-health reminder", description = "Creates a reminder for the authenticated patient.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reminder created"),
            @ApiResponse(responseCode = "400", description = "Invalid reminder details"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Patient role required")
    })
    @PostMapping("/create")
    public ResponseEntity<HealthApiResponse<PreventiveReminder>> createReminder(Authentication auth,
                                                                                @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.createReminder(auth.getName(), request)));
    }

    @Operation(summary = "List my preventive reminders", description = "Returns reminders belonging to the authenticated patient.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reminders returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Patient role required")
    })
    @GetMapping("/list")
    public ResponseEntity<HealthApiResponse<List<PreventiveReminder>>> reminders(Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(service.reminders(auth.getName())));
    }

    @Operation(summary = "Complete a reminder", description = "Marks a patient-owned preventive reminder as completed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reminder completed"),
            @ApiResponse(responseCode = "400", description = "Reminder cannot be completed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Patient role required"),
            @ApiResponse(responseCode = "404", description = "Reminder not found")
    })
    @PatchMapping("/{id}/complete")
    public ResponseEntity<HealthApiResponse<PreventiveReminder>> completeReminder(Authentication auth, @Parameter(description = "Reminder identifier", required = true) @PathVariable String id) {
        return ResponseEntity.ok(HealthApiResponse.success(service.completeReminder(id, auth.getName())));
    }
}
