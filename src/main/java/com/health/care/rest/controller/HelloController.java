package com.health.care.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.health.care.dtos.HealthApiResponse;

@RestController
@RequestMapping("/api")
@Tag(name = "Health Check", description = "Health check and basic API endpoints")
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/hello")
    @Operation(summary = "Hello endpoint", description = "Returns a personalized greeting message for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Greeting message returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - not authenticated")
    })
    public ResponseEntity<HealthApiResponse<String>> hello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String username = authentication.getName();
        logger.debug("Hello endpoint called by user: {}", username);
        logger.info("User '{}' accessed the hello endpoint", username);
        return ResponseEntity.ok(HealthApiResponse.success("Hello, " + username));
    }
}
