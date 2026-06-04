package com.deathz.laborcalc.domain.model;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;

public record SettlementResult(
    Year year,
    BigDecimal totalPrincipal,
    BigDecimal totalAdjustedValue,
    List<MonthlyCompetenceDetail> monthlyDetails
) {}
