package com.health.care.repositories;

import com.health.care.entities.*;
import com.health.care.enums.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends MongoRepository<PaymentTransaction, String> {
    List<PaymentTransaction> findByPayerUsernameOrderByCreatedAtDesc(String username);
    List<PaymentTransaction> findByStatus(PaymentStatus status);
    List<PaymentTransaction> findByReferenceTypeAndReferenceId(String type, String referenceId);
}
