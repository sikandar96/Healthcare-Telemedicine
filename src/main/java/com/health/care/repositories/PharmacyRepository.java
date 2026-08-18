package com.health.care.repositories;

import com.health.care.entities.Pharmacy;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PharmacyRepository extends MongoRepository<Pharmacy, String> {
    List<Pharmacy> findByActiveTrueAndVerifiedTrue();
}
