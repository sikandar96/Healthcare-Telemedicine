package com.health.care.repositories;

import com.health.care.entities.DoctorProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends MongoRepository<DoctorProfile, String> {
    List<DoctorProfile> findByCertifiedTrueAndAvailableTrue();

    Optional<DoctorProfile> findByUsername(String username);
}
