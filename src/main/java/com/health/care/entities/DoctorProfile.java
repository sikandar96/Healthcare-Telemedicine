package com.health.care.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document("doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfile {
    @Id
    private String id;
    private String username;
    private String name;
    private String specialization;
    private String licenseNumber;
    private boolean certified;
    private boolean available;
    private BigDecimal consultationFee;
    private String bio;
}
