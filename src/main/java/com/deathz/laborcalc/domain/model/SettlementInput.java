package com.deathz.laborcalc.domain.model;

import java.time.LocalDate;

public record SettlementInput(
    LocalDate startDate,
    LocalDate endDate,
    LocalDate pandemicStartDate,
    LocalDate pandemicEndDate,
    LocalDate shiftRotationStart,
    Integer shiftRotationInterval,
    Integer additionalPercentage
) {}
