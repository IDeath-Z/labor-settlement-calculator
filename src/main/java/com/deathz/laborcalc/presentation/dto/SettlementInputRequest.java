package com.deathz.laborcalc.presentation.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "SettlementInputRequest", description = "Request DTO for settlement calculation input")
public record SettlementInputRequest(
    @NotNull @Schema(description = "Start date of the settlement period", example = "2026-01-01")
    LocalDate startDate,

    @NotNull @Schema(description = "End date of the settlement period", example = "2026-12-31")
    LocalDate endDate,

    @Schema(description = "Start date of the pandemic period", example = "2020-03-01")
    LocalDate pandemicStartDate,

    @Schema(description = "End date of the pandemic period", example = "2020-12-31")
    LocalDate pandemicEndDate,

    @Schema(description = "Start date of the shift rotation", example = "2026-01-01")
    LocalDate shiftRotationStart,

    @Schema(description = "Interval of the shift rotation, in months", example = "30")
    Integer shiftRotationInterval,

    @NotNull @Min(1) @Max(100) @Schema(description = "Additional percentage for the settlement calculation", example = "40")
    Integer additionalPercentage
) {}
