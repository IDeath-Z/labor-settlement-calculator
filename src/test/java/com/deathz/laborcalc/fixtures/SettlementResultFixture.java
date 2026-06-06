package com.deathz.laborcalc.fixtures;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;

import com.deathz.laborcalc.domain.model.SettlementResult;

public record SettlementResultFixture() {

    public static SettlementResult createWithYearAndTotal() {
        Year year = Year.of(2026);
        BigDecimal totalPrincipal = BigDecimal.valueOf(10000);
        BigDecimal totalAdjustedValue = BigDecimal.valueOf(12000);

        return new SettlementResult(
            year,
            totalPrincipal,
            totalAdjustedValue,
            List.of()
        );
    }
}
