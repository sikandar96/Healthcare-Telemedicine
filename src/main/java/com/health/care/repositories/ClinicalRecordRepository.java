package com.health.care.repositories;

import com.health.care.entities.*;
import com.health.care.enums.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClinicalRecordRepository extends MongoRepository<ClinicalRecord, String> {
    List<ClinicalRecord> findByPatientUsernameOrderByCreatedAtDesc(String username);
    List<ClinicalRecord> findByDoctorUsernameOrderByCreatedAtDesc(String username);
}
