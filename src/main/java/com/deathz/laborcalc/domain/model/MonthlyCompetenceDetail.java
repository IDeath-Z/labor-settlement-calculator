package com.deathz.laborcalc.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyCompetenceDetail(
    LocalDate periodStart,
    LocalDate periodEnd,
    Integer daysWorked,
    BigDecimal currentMinimumWage,
    Integer additionalPercentage,
    BigDecimal fullAdditionalAmount,
    BigDecimal proportionalAdditionalAmount,
    BigDecimal thirteenthSalaryProportion,
    BigDecimal vacationPlusOneThirdProportion,
    BigDecimal fgtsAmount,
    BigDecimal periodTotal,
    BigDecimal accumulatedSelicForMonth,
    BigDecimal selicAmount,
    BigDecimal totalWithSelic
) {
    public static MonthlyCompetenceDetail onlyPeriod(LocalDate start, LocalDate end) {
        return new MonthlyCompetenceDetail(
            start,
            end,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
}
