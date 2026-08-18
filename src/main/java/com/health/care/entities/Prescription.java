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
public class Prescription {
    @Id private String id;
    private String patientUsername;
    private String doctorUsername;
    private String consultationId;
    private List<PrescriptionItem> items;
    private String instructions;
    private PrescriptionStatus status;
    private Instant issuedAt;
}
