package com.deathz.laborcalc.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.deathz.laborcalc.domain.model.MinimumWage;
import com.deathz.laborcalc.domain.model.MonthlyCompetenceDetail;
import com.deathz.laborcalc.domain.model.SelicRate;
import com.deathz.laborcalc.domain.model.SettlementInput;
import com.deathz.laborcalc.domain.model.SettlementResult;

public class SettlementCalculatorService {

    public List<SettlementResult> calculate(SettlementInput input, List<MinimumWage> wageHistory, List<SelicRate> selicHistory) {
        Map<YearMonth, BigDecimal> wageHistoryMap = buildTimelineMap(
            wageHistory, 
            MinimumWage::date, 
            MinimumWage::wageValue
        );

        Map<YearMonth, BigDecimal> selicHistoryMap = buildTimelineMap(
            selicHistory, 
            SelicRate::date, 
            SelicRate::rateValue
        );
        
        YearMonth currentMonth = YearMonth.from(input.startDate());
        YearMonth endMonth = YearMonth.from(input.endDate());
        List<MonthlyCompetenceDetail> allMonths = new ArrayList<>();

        while (!currentMonth.isAfter(endMonth)) {
            
            BigDecimal currentWage = wageHistoryMap.get(currentMonth);
            LocalDate periodStartDate = buildPeriodStartDate(currentMonth, input.startDate());
            LocalDate periodEndDate = buildPeriodEndDate(currentMonth, input.endDate());

            boolean isPandemic = isPandemicMonth(currentMonth, input.pandemicStartDate(), input.pandemicEndDate());
            boolean skipDueToRotation = shouldSkipDueToRotation(currentMonth, input.shiftRotationStart(), input.shiftRotationInterval());

            if (isPandemic || skipDueToRotation) {
                MonthlyCompetenceDetail nullDetail = MonthlyCompetenceDetail.onlyPeriod(periodStartDate, periodEndDate);

                allMonths.add(nullDetail);
                currentMonth = currentMonth.plusMonths(1);
                continue;
            }

            int daysWorked = calculateDaysWorkedInMonth(periodStartDate, periodEndDate);

            BigDecimal proportionalAdditional = calculateProportionalAdditional(currentWage, currentMonth, daysWorked, input.additionalPercentage());
            BigDecimal proportionalThirteenth = calculateProportionalThirteenth(proportionalAdditional);
            BigDecimal proportionalVacationWithThird = calculateProportionalVacationWithThird(proportionalAdditional);
            BigDecimal proportionalFgts = calculateProportionalFgts(proportionalAdditional);
            
            BigDecimal periodTotal = calculatePeriodTotal(proportionalAdditional, proportionalThirteenth, proportionalVacationWithThird, proportionalFgts);
            
            BigDecimal accumulatedSelicForMonth = calculateAccumulatedSelicForMonth(currentMonth, YearMonth.from(input.endDate()), selicHistoryMap);
            BigDecimal totalSelicAmount = calculateSelicAmount(periodTotal, accumulatedSelicForMonth);
            BigDecimal periodTotalWithSelic = calculatePeriodTotalPlusSelic(periodTotal, totalSelicAmount);

            MonthlyCompetenceDetail detail = new MonthlyCompetenceDetail(
                periodStartDate,
                periodEndDate,
                daysWorked,
                currentWage,
                input.additionalPercentage(),
                calculateIntegralAdditional(currentWage, input.additionalPercentage()), 
                proportionalAdditional,
                proportionalThirteenth,
                proportionalVacationWithThird,
                proportionalFgts,
                periodTotal,
                accumulatedSelicForMonth,
                totalSelicAmount,
                periodTotalWithSelic
            );
            
            allMonths.add(detail);
            currentMonth = currentMonth.plusMonths(1);
        }


        return allMonths.stream()
            .collect(Collectors.groupingBy(detail -> Year.of(detail.periodStart().getYear())))
            .entrySet().stream()
            .map(entry -> {
                Year year = entry.getKey();
                List<MonthlyCompetenceDetail> monthsInThisYear = entry.getValue();

                BigDecimal yearTotalPrincipal = monthsInThisYear.stream()
                    .map(MonthlyCompetenceDetail::periodTotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal yearTotalAdjusted = monthsInThisYear.stream()
                    .map(MonthlyCompetenceDetail::totalWithSelic)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                return new SettlementResult(
                    year, 
                    yearTotalPrincipal, 
                    yearTotalAdjusted, 
                    monthsInThisYear
                );
            })
            .sorted(Comparator.comparing(SettlementResult::year))
            .toList();
    }

    private <T> Map<YearMonth, BigDecimal> buildTimelineMap(List<T> historyList, Function<T, LocalDate> dateExtractor, 
        Function<T, BigDecimal> valueExtractor) {
        
        return historyList.stream()
            .collect(Collectors.toMap(
                item -> YearMonth.from(dateExtractor.apply(item)),
                valueExtractor,
                (existingValue, newValue) -> newValue 
            ));
    }

    private boolean isPandemicMonth(YearMonth currentMonth, LocalDate pandemicStart, LocalDate pandemicEnd) {
        if (pandemicStart == null || pandemicEnd == null)
            return false;

        YearMonth start = YearMonth.from(pandemicStart);
        YearMonth end = YearMonth.from(pandemicEnd);
        
        return !currentMonth.isBefore(start) && !currentMonth.isAfter(end);
    }

    private boolean shouldSkipDueToRotation(YearMonth currentMonth, LocalDate rotationStartInput, Integer interval) {
        if (rotationStartInput == null || interval == null || interval <= 0)
            return false;

        YearMonth rotationStart = YearMonth.from(rotationStartInput);

        if (currentMonth.isBefore(rotationStart))
            return true;
        
        int fullCycleLength = interval + 1;
        long monthsBetween = ChronoUnit.MONTHS.between(rotationStart, currentMonth);
        
        return monthsBetween % fullCycleLength != 0;
    }

    private LocalDate buildPeriodStartDate(YearMonth month, LocalDate overallStartDate) {
        return month.equals(YearMonth.from(overallStartDate)) 
            ? overallStartDate 
            : month.atDay(1);
    }

    private LocalDate buildPeriodEndDate(YearMonth month, LocalDate overallEndDate) {
        return month.equals(YearMonth.from(overallEndDate)) 
            ? overallEndDate 
            : month.atEndOfMonth();
    }

    private int calculateDaysWorkedInMonth(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate))
            return 0;

        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1; // + 1 to include the end day
    }

    private BigDecimal calculateIntegralAdditional(BigDecimal currentWage, Integer additionalPercentage) {
        return currentWage
            .multiply(BigDecimal.valueOf(additionalPercentage))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateProportionalAdditional(BigDecimal currentWage, YearMonth currentMonth, int daysWorked, Integer additionalPercentage) {
        
        BigDecimal integralAdditional = calculateIntegralAdditional(currentWage, additionalPercentage);

        if (isFullMonthWorked(daysWorked, currentMonth))
            return integralAdditional;
        
        return integralAdditional
            .multiply(BigDecimal.valueOf(daysWorked))
            .divide(BigDecimal.valueOf(currentMonth.lengthOfMonth()), 2, RoundingMode.HALF_UP);
    }

    private boolean isFullMonthWorked(int daysWorked, YearMonth month) {
        return daysWorked >= month.lengthOfMonth();
    }

    private BigDecimal calculateProportionalThirteenth(BigDecimal proportionalAdditional) {
        if (proportionalAdditional.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        return proportionalAdditional.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateProportionalVacationWithThird(BigDecimal proportionalAdditional) {
        if (proportionalAdditional.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal vacationAmount = proportionalAdditional.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal constitutionalThird = vacationAmount.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);

        return vacationAmount.add(constitutionalThird);
    }

    private BigDecimal calculateProportionalFgts(BigDecimal baseAmount) {
        if (baseAmount.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        return baseAmount
                .multiply(BigDecimal.valueOf(8))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePeriodTotal(BigDecimal proportionalAdditional, BigDecimal proportionalThirteenth, 
        BigDecimal proportionalVacationWithThird, BigDecimal proportionalFgts) {
        return proportionalAdditional
            .add(proportionalThirteenth)
            .add(proportionalVacationWithThird)
            .add(proportionalFgts);
    }

    private BigDecimal calculateAccumulatedSelicForMonth(YearMonth startMonth, YearMonth endMonth, Map<YearMonth, BigDecimal> selicMap) {
        BigDecimal accumulated = BigDecimal.ONE;
        YearMonth current = startMonth;

        while (!current.isAfter(endMonth)) {
            BigDecimal monthlyRate = selicMap.getOrDefault(current, BigDecimal.ZERO);

            BigDecimal factor = BigDecimal.ONE.add(
                monthlyRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)
            );

            accumulated = accumulated.multiply(factor);
            current = current.plusMonths(1);
        }

        return accumulated
            .subtract(BigDecimal.ONE)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSelicAmount(BigDecimal baseAmount, BigDecimal accumulatedSelic) {
        
        if (accumulatedSelic.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal selicFactor = accumulatedSelic.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        return baseAmount
            .multiply(selicFactor)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePeriodTotalPlusSelic(BigDecimal periodTotal, BigDecimal selicAmount) {
        return periodTotal.add(selicAmount);
    }
}
