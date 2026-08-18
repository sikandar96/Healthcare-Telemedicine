package com.health.care.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.health.care.enums.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CampaignRequest(@NotBlank String sponsor, @NotBlank String title, @NotBlank String description, @NotNull @DecimalMin("0.00") BigDecimal budget, @NotNull LocalDate startDate, @NotNull LocalDate endDate) {}
