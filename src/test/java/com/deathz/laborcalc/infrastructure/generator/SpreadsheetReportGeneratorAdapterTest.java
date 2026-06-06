package com.deathz.laborcalc.infrastructure.generator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.deathz.laborcalc.domain.enums.ReportFormat;

public class SpreadsheetReportGeneratorAdapterTest {

    private SpreadsheetReportGeneratorAdapter spreadsheetReportGeneratorAdapter;

    @BeforeEach
    void setUp() {
        spreadsheetReportGeneratorAdapter = new SpreadsheetReportGeneratorAdapter();
    }

    @Test
    @DisplayName("Should support SPREADSHEET format")
    void shouldSupportSpreadsheetFormat() {
        assertTrue(spreadsheetReportGeneratorAdapter.supports(ReportFormat.SPREADSHEET));
    }

    @Test
    @DisplayName("Should not support unsupported format")
    void shouldNotSupportUnsupportedFormat() {
        assertFalse(spreadsheetReportGeneratorAdapter.supports(ReportFormat.UNSUPPORTED));
    }
}
