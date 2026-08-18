package com.health.care.exceptions;

import com.health.care.dtos.HealthApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<HealthApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        String message = String.join("; ", errors);
        logger.warn("Validation failed: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HealthApiResponse.error(message));
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<HealthApiResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("Bad credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(HealthApiResponse.error("Invalid credentials"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<HealthApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(HealthApiResponse.error("Access denied"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<HealthApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HealthApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    protected ResponseEntity<HealthApiResponse<Object>> handleExpiredJwt(ExpiredJwtException ex) {
        logger.warn("Expired JWT: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(HealthApiResponse.error("Token expired"));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<HealthApiResponse<Object>> handleAll(Exception ex) {
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(HealthApiResponse.error("Internal server error"));
    }
}

