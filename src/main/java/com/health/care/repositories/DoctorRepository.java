package com.health.care.repositories;

import com.health.care.entities.DoctorProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DoctorRepository extends MongoRepository<DoctorProfile, String> {
    List<DoctorProfile> findByCertifiedTrueAndAvailableTrue();
}
