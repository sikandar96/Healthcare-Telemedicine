package com.health.care.entities;

import com.health.care.enums.ReminderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Document("preventive_reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreventiveReminder {
    @Id
    private String id;
    private String username;
    private ReminderType type;
    private String title;
    private String details;
    private LocalDate dueDate;
    private boolean completed;
    private boolean notified;
    private Instant createdAt;
}
