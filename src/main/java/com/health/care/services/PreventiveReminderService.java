package com.health.care.services;

import com.health.care.dtos.ReminderRequest;
import com.health.care.entities.PreventiveReminder;
import com.health.care.repositories.PreventiveReminderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class PreventiveReminderService {

    private static final Logger logger = LoggerFactory.getLogger(PreventiveReminderService.class);
    private final PreventiveReminderRepository repository;


    public PreventiveReminderService(PreventiveReminderRepository repository) {
        this.repository = repository;
    }

    public PreventiveReminder createReminder(String username, ReminderRequest request) {
        logger.info("Creating preventive reminder for user '{}' with due date '{}'", username, request.dueDate());
        PreventiveReminder saved = repository.save(new PreventiveReminder(null, username, request.type(), request.title(), request.details(),
                request.dueDate(), false, false, Instant.now()));
        logger.info("Preventive reminder '{}' created for user '{}'", saved.getId(), username);
        return saved;
    }

    public List<PreventiveReminder> reminders(String username) {
        return repository.findByUsernameOrderByDueDateAsc(username);
    }

    public PreventiveReminder completeReminder(String id, String username) {
        PreventiveReminder reminder = repository.findById(id)
                .filter(r -> r.getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        logger.info("Completing preventive reminder '{}' for user '{}'", id, username);
        reminder.setCompleted(true);
        PreventiveReminder saved = repository.save(reminder);
        logger.info("Preventive reminder '{}' completed", saved.getId());
        return saved;
    }

    @Scheduled(cron = "${app.reminders.cron:0 0 8 * * *}")
    public void markDueRemindersAsNotified() {
        List<PreventiveReminder> dueReminders = repository.findByDueDateLessThanEqualAndCompletedFalseAndNotifiedFalse(LocalDate.now());
        logger.debug("Processing {} due preventive reminders", dueReminders.size());
        dueReminders.forEach(reminder -> {
            reminder.setNotified(true);
            repository.save(reminder);
        });
        if (!dueReminders.isEmpty()) {
            logger.info("Marked {} preventive reminders as notified", dueReminders.size());
        }
    }
}
