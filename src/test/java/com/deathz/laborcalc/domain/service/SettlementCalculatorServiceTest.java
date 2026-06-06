package com.deathz.laborcalc.domain.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvFileSource;

import com.deathz.laborcalc.domain.model.MinimumWage;
import com.deathz.laborcalc.domain.model.MonthlyCompetenceDetail;
import com.deathz.laborcalc.domain.model.SelicRate;
import com.deathz.laborcalc.domain.model.SettlementInput;
import com.deathz.laborcalc.domain.service.fixtures.SettlementInputFixture;

import converters.BigDecimalConverter;
import converters.LocalDateConverter;

public class SettlementCalculatorServiceTest {

    private SettlementCalculatorService settlementCalculatorService;

    private static final String CSV_PATH = "/service/valid_results.csv";

    @BeforeEach
    void setUp() {
        settlementCalculatorService = new SettlementCalculatorService();
    }

    @ParameterizedTest
    @CsvFileSource(resources = CSV_PATH, numLinesToSkip = 1)
    @DisplayName("Should calculate all columns correctly for various input scenarios")
    void shouldCalculateAllColumnsCorrectly(
        @ConvertWith(LocalDateConverter.class) LocalDate startDate,
        @ConvertWith(LocalDateConverter.class) LocalDate endDate,
        Integer days,
        @ConvertWith(BigDecimalConverter.class) BigDecimal wage,
        Integer additionalPct,
        @ConvertWith(BigDecimalConverter.class) BigDecimal expectedIntegral,
        @ConvertWith(BigDecimalConverter.class) BigDecimal expectedProportional,
        @ConvertWith(BigDecimalConverter.class) BigDecimal expectedThirteenth,
        @ConvertWith(BigDecimalConverter.class) BigDecimal expectedVacation,
        @ConvertWith(BigDecimalConverter.class) BigDecimal expectedFgts,
        @ConvertWith(BigDecimalConverter.class) BigDecimal expectedTotal,
        @ConvertWith(BigDecimalConverter.class) BigDecimal expectedAccumulatedSelic,
        @ConvertWith(BigDecimalConverter.class) BigDecimal expectedSelicAmount,
        @ConvertWith(BigDecimalConverter.class) BigDecimal expectedTotalWithSelic
    ) {
        SettlementInput input = new SettlementInput(
            startDate,
            endDate,
            null,
            null,
            null,
            null,
            additionalPct
        );

        List<MinimumWage> wages = List.of(new MinimumWage(startDate, wage));
        List<SelicRate> selic = List.of(new SelicRate(startDate, expectedAccumulatedSelic));

        MonthlyCompetenceDetail detail = settlementCalculatorService.calculate(input, wages, selic).getFirst().monthlyDetails().getFirst();

        assertAll(
            () -> assertEquals(startDate, detail.periodStart()),
            () -> assertEquals(endDate, detail.periodEnd()),
            () -> assertEquals(days, detail.daysWorked()),
            () -> assertEquals(wage, detail.currentMinimumWage()),
            () -> assertEquals(additionalPct, detail.additionalPercentage()),
            () -> assertEquals(expectedIntegral, detail.fullAdditionalAmount()),
            () -> assertEquals(expectedProportional, detail.proportionalAdditionalAmount()),
            () -> assertEquals(expectedThirteenth, detail.thirteenthSalaryProportion()),
            () -> assertEquals(expectedVacation, detail.vacationPlusOneThirdProportion()),
            () -> assertEquals(expectedFgts, detail.fgtsAmount()),
            () -> assertEquals(expectedTotal, detail.periodTotal()),
            () -> assertEquals(expectedAccumulatedSelic, detail.accumulatedSelicForMonth()),
            () -> assertEquals(expectedSelicAmount, detail.selicAmount()),
            () -> assertEquals(expectedTotalWithSelic, detail.totalWithSelic())
        );
    }

    @Test
    @DisplayName("Should return null for all calculated fields when shift rotation is provided")
    void shouldReturnNullWhenShiftRotationIsAfterPeriod() {
        SettlementInput input = SettlementInputFixture.createWithShiftRotationAfter();
        List<MinimumWage> wages = List.of(new MinimumWage(input.startDate(), BigDecimal.valueOf(1500)));
        List<SelicRate> selic = List.of(new SelicRate(input.startDate(), BigDecimal.valueOf(0.05)));

        MonthlyCompetenceDetail detail = settlementCalculatorService.calculate(input, wages, selic).getFirst().monthlyDetails().getFirst();

        assertAll(
            () -> assertEquals(input.startDate(), detail.periodStart()),
            () -> assertEquals(input.endDate(), detail.periodEnd()),
            () -> assertNull(detail.daysWorked()),
            () -> assertNull(detail.currentMinimumWage()),
            () -> assertNull(detail.additionalPercentage()),
            () -> assertNull(detail.fullAdditionalAmount()),
            () -> assertNull(detail.proportionalAdditionalAmount()),
            () -> assertNull(detail.thirteenthSalaryProportion()),
            () -> assertNull(detail.vacationPlusOneThirdProportion()),
            () -> assertNull(detail.fgtsAmount()),
            () -> assertNull(detail.periodTotal()),
            () -> assertNull(detail.accumulatedSelicForMonth()),
            () -> assertNull(detail.selicAmount()),
            () -> assertNull(detail.totalWithSelic())
        );
    }

    @Test
    @DisplayName("Should return null for all calculated fields when pandemic period covers entire settlement")
    void shouldReturnNullWhenPandemicPeriodCoversEntireSettlement() {
        SettlementInput input = SettlementInputFixture.createWithPandemicPeriod();
        List<MinimumWage> wages = List.of(new MinimumWage(input.startDate(), BigDecimal.valueOf(1500)));
        List<SelicRate> selic = List.of(new SelicRate(input.startDate(), BigDecimal.valueOf(0.05)));

        MonthlyCompetenceDetail detail = settlementCalculatorService.calculate(input, wages, selic).getFirst().monthlyDetails().getFirst();

        assertAll(
            () -> assertEquals(input.startDate(), detail.periodStart()),
            () -> assertEquals(input.endDate(), detail.periodEnd()),
            () -> assertNull(detail.daysWorked()),
            () -> assertNull(detail.currentMinimumWage()),
            () -> assertNull(detail.additionalPercentage()),
            () -> assertNull(detail.fullAdditionalAmount()),
            () -> assertNull(detail.proportionalAdditionalAmount()),
            () -> assertNull(detail.thirteenthSalaryProportion()),
            () -> assertNull(detail.vacationPlusOneThirdProportion()),
            () -> assertNull(detail.fgtsAmount()),
            () -> assertNull(detail.periodTotal()),
            () -> assertNull(detail.accumulatedSelicForMonth()),
            () -> assertNull(detail.selicAmount()),
            () -> assertNull(detail.totalWithSelic())
        );
    }
}
