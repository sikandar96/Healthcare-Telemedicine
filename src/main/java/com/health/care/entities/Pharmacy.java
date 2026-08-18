package com.health.care.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document("pharmacies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pharmacy {
    @Id
    private String id;
    private String name;
    private String address;
    private String phone;
    private boolean verified;
    private boolean active;
    private BigDecimal commissionRate;
}
