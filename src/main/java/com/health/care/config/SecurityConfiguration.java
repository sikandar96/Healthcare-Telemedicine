package com.health.care.config;

import com.health.care.security.MongoUserDetailsService;
import com.health.care.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MongoUserDetailsService mongoUserDetailsService;
    private final PasswordEncoder passwordEncoder;

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
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // Authentication and operational documentation are public.
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
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
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\",\"data\":null}");
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
