package com.health.care.dtos;

import java.math.BigDecimal;

public record RevenueSummary(
        BigDecimal consultationGross,
        BigDecimal consultationCommission,
        BigDecimal pharmacyGross,
        BigDecimal pharmacyCommission,
        BigDecimal sponsoredProgramRevenue,
        BigDecimal totalRevenue) {}