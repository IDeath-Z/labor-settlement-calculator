package com.deathz.laborcalc.domain.service.fixtures;

import java.time.LocalDate;

import com.deathz.laborcalc.domain.model.SettlementInput;


public record SettlementInputFixture() {

    public static SettlementInput createWithPandemicPeriod() {
        LocalDate startDate = LocalDate.of(2020, 9, 1);
        LocalDate endDate = LocalDate.of(2020, 9, 30);
        LocalDate pandemicStartDate = LocalDate.of(2020, 1, 1);
        LocalDate pandemicEndDate = LocalDate.of(2022, 12, 31);
        
        return new SettlementInput(
            startDate,
            endDate,
            pandemicStartDate,
            pandemicEndDate,
            null,
            null,
            40
        );
    }

    public static SettlementInput createWithShiftRotationAfter() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);
        LocalDate shiftRotationStart = LocalDate.of(2026, 2, 1);
        Integer shiftRotationInterval = 3;
        
        return new SettlementInput(
            startDate,
            endDate,
            null,
            null,
            shiftRotationStart,
            shiftRotationInterval,
            40
        );
    }
}
