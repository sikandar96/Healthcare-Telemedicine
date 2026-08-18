package com.health.care.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import com.health.care.enums.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
@CompoundIndex(name = "payer_idempotency_key_unique", def = "{'payerUsername': 1, 'idempotencyKey': 1}", unique = true, partialFilter = "{'idempotencyKey': {$exists: true, $type: 'string'}}")
public class PaymentTransaction {
    @Id private String id;
    private String payerUsername;
    private String idempotencyKey;
    private String referenceType;
    private String referenceId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String providerReference;
    private Instant createdAt;
    private Instant updatedAt;
}
