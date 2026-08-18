package com.health.care.repositories;

import com.health.care.entities.PreventiveReminder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PreventiveReminderRepository extends MongoRepository<PreventiveReminder, String> {
    List<PreventiveReminder> findByUsernameOrderByDueDateAsc(String username);
    List<PreventiveReminder> findByDueDateLessThanEqualAndCompletedFalseAndNotifiedFalse(java.time.LocalDate date);
}