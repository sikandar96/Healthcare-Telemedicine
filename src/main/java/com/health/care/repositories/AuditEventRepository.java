package com.health.care.repositories;

import com.health.care.entities.*;
import com.health.care.enums.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
    List<AuditEvent> findByActorOrderByOccurredAtDesc(String actor);
    long countByAction(String action);
}
