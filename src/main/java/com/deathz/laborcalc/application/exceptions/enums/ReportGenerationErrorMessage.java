package com.deathz.laborcalc.application.exceptions.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportGenerationErrorMessage {

    ERROR_GENERATING_REPORT("Failed to generate report"),
    SPREADSHEET_GENERATING_ERROR("Failed to generate spreadsheet report");
    
    private final String message;
}
