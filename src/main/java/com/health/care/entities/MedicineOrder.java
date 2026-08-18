package com.health.care.entities;

import com.health.care.enums.MedicineOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document("medicine_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineOrder {
    @Id
    private String id;
    private String patientUsername;
    private String pharmacyId;
    private String pharmacyName;
    private List<MedicineOrderItem> items;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal total;
    private BigDecimal pharmacyCommission;
    private MedicineOrderStatus status;
    private String deliveryAddress;
    private Instant createdAt;
    private Instant updatedAt;
}
