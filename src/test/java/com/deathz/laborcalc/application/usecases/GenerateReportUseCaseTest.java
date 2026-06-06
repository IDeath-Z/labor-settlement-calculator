package com.deathz.laborcalc.application.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.deathz.laborcalc.application.exceptions.BusinessRuleException;
import com.deathz.laborcalc.application.exceptions.ReportGenerationException;
import com.deathz.laborcalc.domain.enums.ReportFormat;
import com.deathz.laborcalc.domain.ports.ReportGeneratorPort;
import com.deathz.laborcalc.fixtures.SettlementResultFixture;

@ExtendWith(MockitoExtension.class)
public class GenerateReportUseCaseTest {

    @Mock
    private ReportGeneratorPort reportGenerator;


    private GenerateReportUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateReportUseCase(List.of(reportGenerator));
    }

    @Test
    @DisplayName("Should throw when settlement results is empty")
    void shouldThrowWhenSettlementResultsIsEmpty() {
        assertThrows(BusinessRuleException.class, () -> useCase.execute(List.of(), ReportFormat.SPREADSHEET));
    }

    @Test
    @DisplayName("Should throw when format is unsupported")
    void shouldThrowWhenFormatIsUnsupported() {
        when(reportGenerator.supports(any())).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> useCase.execute(List.of(SettlementResultFixture.createWithYearAndTotal()), ReportFormat.UNSUPPORTED));
    }

    @Test
    @DisplayName("Should throw when report generation fails")
    void shouldThrowWhenReportGenerationFails() {
        when(reportGenerator.supports(any())).thenReturn(true);
        when(reportGenerator.generate(any())).thenReturn(new byte[0]);

        assertThrows(ReportGenerationException.class, () -> useCase.execute(List.of(SettlementResultFixture.createWithYearAndTotal()), ReportFormat.SPREADSHEET));
    }

}
