package com.health.care.repositories;

import com.health.care.entities.MedicineOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MedicineOrderRepository extends MongoRepository<MedicineOrder, String> {
    List<MedicineOrder> findByPatientUsernameOrderByCreatedAtDesc(String username);
}
