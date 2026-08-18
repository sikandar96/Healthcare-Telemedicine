package com.health.care.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret = "change-me-change-me-change-me-change-me";
    private long expirationMs = 86_400_000L;
    private String header = "Authorization";
    private String prefix = "Bearer";
    private String issuer = "healthcare-telemedicine";
}
