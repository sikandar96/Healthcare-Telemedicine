package com.health.care.rest.controller;

import com.health.care.dtos.HealthApiResponse;
import com.health.care.dtos.ReminderRequest;
import com.health.care.entities.PreventiveReminder;
import com.health.care.services.PreventiveReminderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class PreventiveReminderController {

    private final PreventiveReminderService service;


    public PreventiveReminderController(PreventiveReminderService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<HealthApiResponse<PreventiveReminder>> createReminder(Authentication auth,
                                                                                @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.ok(HealthApiResponse.success(service.createReminder(auth.getName(), request)));
    }

    @GetMapping("/list")
    public ResponseEntity<HealthApiResponse<List<PreventiveReminder>>> reminders(Authentication auth) {
        return ResponseEntity.ok(HealthApiResponse.success(service.reminders(auth.getName())));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<HealthApiResponse<PreventiveReminder>> completeReminder(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(HealthApiResponse.success(service.completeReminder(id, auth.getName())));
    }
}
