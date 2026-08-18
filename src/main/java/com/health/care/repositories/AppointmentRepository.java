package com.health.care.repositories;

import com.health.care.entities.*;
import com.health.care.enums.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByPatientUsernameOrderByStartAtDesc(String username);
    List<Appointment> findByDoctorIdOrderByStartAtDesc(String doctorId);
    boolean existsByDoctorIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(String doctorId, List<AppointmentStatus> statuses, LocalDateTime endAt, LocalDateTime startAt);
}
