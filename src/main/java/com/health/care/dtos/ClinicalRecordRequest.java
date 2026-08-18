package com.health.care.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.health.care.enums.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ClinicalRecordRequest(@NotBlank String patientUsername, String consultationId, @NotBlank String diagnosis, String notes, List<String> attachmentUrls, boolean patientConsent) {}
