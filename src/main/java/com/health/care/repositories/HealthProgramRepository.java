package com.health.care.repositories;

import com.health.care.entities.HealthProgram;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HealthProgramRepository extends MongoRepository<HealthProgram, String> {
    List<HealthProgram> findByPublishedTrueOrderByPublishedAtDesc();
}
