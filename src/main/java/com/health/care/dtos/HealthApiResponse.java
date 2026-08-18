package com.health.care.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public HealthApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public HealthApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> HealthApiResponse<T> success(T data) {
        return new HealthApiResponse<>(true, null, data);
    }

    public static <T> HealthApiResponse<T> success(String message, T data) {
        return new HealthApiResponse<>(true, message, data);
    }

    public static <T> HealthApiResponse<T> error(String message) {
        return new HealthApiResponse<>(false, message, null);
    }

   }

