package com.health.care.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.health.care.dtos.HealthApiResponse;
import com.health.care.security.MongoUserDetailsService;
import com.health.care.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MongoUserDetailsService mongoUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter,
                                 MongoUserDetailsService mongoUserDetailsService,
                                 PasswordEncoder passwordEncoder) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.mongoUserDetailsService = mongoUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // Authentication and operational documentation are public.
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/forgot-password",
                                "/api/auth/request-otp",
                                "/api/auth/verify-otp",
                                "/api/auth/reset-password",
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html")
                        .permitAll()

                        // Only administrators can assign persisted roles in MongoDB.
                        .requestMatchers(HttpMethod.PUT, "/api/auth/users/*/roles")
                        .hasRole("ADMIN")

                        // DoctorController.
                        .requestMatchers(HttpMethod.GET, "/api/doctors/available")
                        .hasAnyRole("PATIENT", "DOCTOR", "PHARMACY_PARTNER", "HEALTH_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/doctors/register")
                        .hasAnyRole("HEALTH_MANAGER", "ADMIN")

                        // ConsultationController.
                        .requestMatchers(HttpMethod.POST, "/api/consultations/book")
                        .hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/consultations/my")
                        .hasAnyRole("PATIENT", "DOCTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/consultations/*/status")
                        .hasAnyRole("PATIENT", "DOCTOR")

                        // PharmacyController.
                        .requestMatchers(HttpMethod.GET, "/api/pharmacies/available")
                        .hasAnyRole("PATIENT", "DOCTOR", "PHARMACY_PARTNER", "HEALTH_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pharmacies/add")
                        .hasAnyRole("HEALTH_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pharmacies/medicine-orders")
                        .hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/pharmacies/medicine-orders")
                        .hasRole("PATIENT")

                        // PreventiveReminderController.
                        .requestMatchers("/api/reminders/**")
                        .hasRole("PATIENT")

                        // HealthcareController consolidated routes mirror the dedicated controller policy.
                        .requestMatchers(HttpMethod.GET, "/api/healthcare/doctors", "/api/healthcare/pharmacies")
                        .hasAnyRole("PATIENT", "DOCTOR", "PHARMACY_PARTNER", "HEALTH_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/healthcare/doctors", "/api/healthcare/pharmacies")
                        .hasAnyRole("HEALTH_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/healthcare/consultations", "/api/healthcare/medicine-orders")
                        .hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/healthcare/consultations")
                        .hasAnyRole("PATIENT", "DOCTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/healthcare/consultations/*/status")
                        .hasAnyRole("PATIENT", "DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/healthcare/medicine-orders")
                        .hasRole("PATIENT")
                        .requestMatchers("/api/healthcare/reminders/**")
                        .hasRole("PATIENT")

                        // New platform workflows.
                        .requestMatchers(HttpMethod.GET, "/api/platform/campaigns/active")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/platform/doctor-verifications")
                        .hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/platform/doctor-verifications/pending")
                        .hasAnyRole("HEALTH_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/platform/doctor-verifications/*")
                        .hasAnyRole("HEALTH_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/platform/appointments")
                        .hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/platform/appointments/mine")
                        .hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/platform/appointments/doctor/mine")
                        .hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/platform/appointments/doctor/*")
                        .hasAnyRole("HEALTH_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/platform/appointments/*/status")
                        .hasAnyRole("PATIENT", "DOCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/platform/clinical-records")
                        .hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/platform/clinical-records/mine")
                        .hasAnyRole("PATIENT", "DOCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/platform/prescriptions")
                        .hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/platform/prescriptions/mine")
                        .hasAnyRole("PATIENT", "DOCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/platform/inventory")
                        .hasAnyRole("PHARMACY_PARTNER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/platform/inventory/*")
                        .hasAnyRole("PHARMACY_PARTNER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/platform/inventory/*")
                        .hasAnyRole("PATIENT", "DOCTOR", "PHARMACY_PARTNER", "HEALTH_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/platform/payments")
                        .hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/platform/payments/mine")
                        .authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/platform/payments/*")
                        .hasAnyRole("HEALTH_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/platform/notifications", "/api/platform/consents")
                        .authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/platform/notifications/*/read")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/platform/consents")
                        .hasRole("PATIENT")
                        .requestMatchers(HttpMethod.PATCH, "/api/platform/consents/*/revoke")
                        .hasRole("PATIENT")
                        .requestMatchers(HttpMethod.POST, "/api/platform/campaigns")
                        .hasAnyRole("HEALTH_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/platform/audit/count/*")
                        .hasRole("ADMIN")

                        // The remaining health-program and revenue endpoints are owned by HealthcareController.
                        .requestMatchers(HttpMethod.GET, "/api/healthcare/health-programs")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/healthcare/health-programs")
                        .hasAnyRole("HEALTH_MANAGER", "ADMIN")
                        .requestMatchers("/api/healthcare/revenue/**")
                        .hasAnyRole("HEALTH_MANAGER", "ADMIN")

                        .anyRequest()
                        .authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> writeSecurityError(
                response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication is required");
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> writeSecurityError(
                response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
    }

    private void writeSecurityError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(HealthApiResponse.error(message)));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        String configuredOrigins = System.getenv().getOrDefault(
                "APP_CORS_ALLOWED_ORIGINS",
                "http://localhost:3000,http://127.0.0.1:3000,http://localhost:5173,http://127.0.0.1:5173,http://localhost:4173,http://127.0.0.1:4173");
        configuration.setAllowedOrigins(Arrays.stream(configuredOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Accept", "Origin", "X-Requested-With", "Cache-Control", "Content-Type", "Authorization", "Idempotency-Key"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(mongoUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
