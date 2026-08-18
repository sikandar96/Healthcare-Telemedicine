package com.health.care.entities;

import com.health.care.enums.ConsultationStatus;
import com.health.care.enums.ConsultationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document("consultations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consultation {
    @Id
    private String id;
    private String patientUsername;
    private String doctorId;
    private String doctorName;
    private ConsultationType type;
    private ConsultationStatus status;
    private Instant scheduledAt;
    private Instant startedAt;
    private Instant completedAt;
    private BigDecimal fee;
    private BigDecimal platformCommission;
    private BigDecimal doctorPayout;
    private String meetingRoomUrl;
    private Instant createdAt;
}
