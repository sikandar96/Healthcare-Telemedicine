package com.health.care.repositories;

import com.health.care.entities.Consultation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConsultationRepository extends MongoRepository<Consultation, String> {
    List<Consultation> findByPatientUsernameOrderByScheduledAtDesc(String username);

    List<Consultation> findByDoctorIdOrderByScheduledAtDesc(String doctorId);
}
