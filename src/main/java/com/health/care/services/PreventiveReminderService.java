package com.health.care.services;

import com.health.care.dtos.ReminderRequest;
import com.health.care.entities.PreventiveReminder;
import com.health.care.repositories.PreventiveReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class PreventiveReminderService {

    private final PreventiveReminderRepository repository;


    public PreventiveReminderService(PreventiveReminderRepository repository) {
        this.repository = repository;
    }

    public PreventiveReminder createReminder(String username, ReminderRequest request) {
        return repository.save(new PreventiveReminder(null, username, request.type(), request.title(), request.details(),
                request.dueDate(), false, false, Instant.now()));
    }

    public List<PreventiveReminder> reminders(String username) {
        return repository.findByUsernameOrderByDueDateAsc(username);
    }

    public PreventiveReminder completeReminder(String id, String username) {
        PreventiveReminder reminder = repository.findById(id)
                .filter(r -> r.getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        reminder.setCompleted(true);
        return repository.save(reminder);
    }

    @Scheduled(cron = "${app.reminders.cron:0 0 8 * * *}")
    public void markDueRemindersAsNotified() {
        repository.findByDueDateLessThanEqualAndCompletedFalseAndNotifiedFalse(LocalDate.now())
                .forEach(reminder -> {
                    reminder.setNotified(true);
                    repository.save(reminder);
                });
    }
}
