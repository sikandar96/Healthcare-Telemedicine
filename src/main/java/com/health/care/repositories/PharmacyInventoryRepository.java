package com.health.care.repositories;

import com.health.care.entities.*;
import com.health.care.enums.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PharmacyInventoryRepository extends MongoRepository<PharmacyInventory, String> {
    List<PharmacyInventory> findByPharmacyIdOrderByMedicineNameAsc(String pharmacyId);
    Optional<PharmacyInventory> findByPharmacyIdAndSku(String pharmacyId, String sku);
}
