package com.health.care.security;

import java.util.Locale;

public enum AppRole {
    PATIENT,
    DOCTOR,
    PHARMACY_PARTNER,
    HEALTH_MANAGER,
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }

    public static AppRole from(String value) {
        if (value == null || value.isBlank()) {
            return PATIENT;
        }
        String normalized = value.toUpperCase(Locale.ROOT).replace("ROLE_", "");
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported role: " + value);
        }
    }
}
