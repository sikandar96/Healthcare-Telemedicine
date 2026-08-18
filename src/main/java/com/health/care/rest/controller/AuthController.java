package com.health.care.rest.controller;

import com.health.care.dtos.AuthRequest;
import com.health.care.dtos.AuthResponse;
import com.health.care.dtos.HealthApiResponse;
import com.health.care.dtos.RoleUpdateRequest;
import com.health.care.security.MongoUserDetailsService;
import com.health.care.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints for user login and registration")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final MongoUserDetailsService mongoUserDetailsService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, MongoUserDetailsService mongoUserDetailsService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.mongoUserDetailsService = mongoUserDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate a user with username and password to obtain a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Bad request - missing or invalid parameters")
    })
    public ResponseEntity<HealthApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        logger.debug("Login attempt for username: {}", request.username());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails principal = (UserDetails) authentication.getPrincipal();
            assert principal != null;
            List<String> roles = principal.getAuthorities().stream().map(Object::toString).toList();
            String token = jwtService.generateToken(principal.getUsername(), roles);
            logger.info("User '{}' logged in successfully", request.username());
            AuthResponse payload = new AuthResponse(token, "Bearer", jwtService.getExpirationMs(), roles);
            return ResponseEntity.ok(HealthApiResponse.success(payload));
        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt for username: {} - Invalid credentials", request.username());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for username: {}", request.username(), e);
            throw e;
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account with username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful, JWT token returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid or duplicate username"),
            @ApiResponse(responseCode = "409", description = "Conflict - user already exists")
    })
    public ResponseEntity<HealthApiResponse<AuthResponse>> register(@Valid @RequestBody AuthRequest request) {
        logger.debug("Registration attempt for username: {}", request.username());
        try {
            UserDetails userDetails = mongoUserDetailsService.register(request.username(), request.password());
            List<String> roles = userDetails.getAuthorities().stream().map(Object::toString).toList();
            String token = jwtService.generateToken(userDetails.getUsername(), roles);
            logger.info("New user '{}' registered successfully", request.username());
            AuthResponse payload = new AuthResponse(token, "Bearer", jwtService.getExpirationMs(), roles);
            return ResponseEntity.ok(HealthApiResponse.success(payload));
        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed for username: {} - {}", request.username(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during registration for username: {}", request.username(), e);
            throw e;
        }
    }

    @PutMapping("/users/{username}/roles")
    public ResponseEntity<HealthApiResponse<List<String>>> updateRoles(
            @PathVariable String username,
            @Valid @RequestBody RoleUpdateRequest request) {
        UserDetails userDetails = mongoUserDetailsService.updateRoles(username, request.roles());
        List<String> roles = userDetails.getAuthorities().stream().map(Object::toString).toList();
        return ResponseEntity.ok(HealthApiResponse.success(roles));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info", description = "Retrieve information about the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current user information retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - not authenticated")
    })
    public ResponseEntity<HealthApiResponse<String>> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String username = authentication.getName();
        logger.debug("User info request for: {}", username);
        return ResponseEntity.ok(HealthApiResponse.success(username));
    }
}
