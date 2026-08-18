package com.health.care.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.health.care.enums.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class AuditEvent {
    @Id private String id;
    private String actor;
    private String action;
    private String entityType;
    private String entityId;
    private String correlationId;
    private String outcome;
    private Instant occurredAt;
}
