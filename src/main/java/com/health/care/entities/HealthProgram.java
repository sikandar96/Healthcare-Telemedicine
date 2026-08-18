package com.health.care.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document("health_programs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthProgram {
    @Id
    private String id;
    private String title;
    private String category;
    private String content;
    private String sponsorName;
    private boolean sponsored;
    private BigDecimal sponsorshipFee;
    private boolean published;
    private Instant publishedAt;
}
