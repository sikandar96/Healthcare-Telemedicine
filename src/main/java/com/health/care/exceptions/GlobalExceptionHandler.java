package com.health.care.exceptions;

import com.health.care.dtos.HealthApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.validation.ConstraintViolationException;
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<HealthApiResponse<Object>> handleMalformedRequest(HttpMessageNotReadableException ex) {
        logger.warn("Malformed request body: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HealthApiResponse.error("Malformed request body"));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    protected ResponseEntity<HealthApiResponse<Object>> handleRequestParameterException(Exception ex) {
        logger.warn("Invalid request parameter: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HealthApiResponse.error("Invalid request parameter"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<HealthApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        logger.warn("Constraint validation failed: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HealthApiResponse.error(message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<HealthApiResponse<Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        logger.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(HealthApiResponse.error("Resource already exists or violates a data constraint"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    protected ResponseEntity<HealthApiResponse<Object>> handleNotFound(NoResourceFoundException ex) {
        logger.warn("Resource not found: {}", ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(HealthApiResponse.error("Resource not found"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<HealthApiResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        logger.warn("HTTP method not supported: {}", ex.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(HealthApiResponse.error("HTTP method not supported"));
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

