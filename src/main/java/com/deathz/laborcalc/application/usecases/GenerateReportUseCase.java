package com.deathz.laborcalc.application.usecases;

import java.util.List;

import com.deathz.laborcalc.application.exceptions.BusinessRuleException;
import com.deathz.laborcalc.application.exceptions.ReportGenerationException;
import com.deathz.laborcalc.application.exceptions.enums.BusinessRuleErrorMessage;
import com.deathz.laborcalc.application.exceptions.enums.ReportGenerationErrorMessage;
import com.deathz.laborcalc.domain.enums.ReportFormat;
import com.deathz.laborcalc.domain.model.SettlementResult;
import com.deathz.laborcalc.domain.ports.ReportGeneratorPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenerateReportUseCase {

    private final List<ReportGeneratorPort> reportGenerators;

    public byte[] execute(List<SettlementResult> results, ReportFormat format) {
        if (results == null || results.isEmpty())
            throw new BusinessRuleException(BusinessRuleErrorMessage.SETTLEMENT_EMPTY.getMessage());

        ReportGeneratorPort generator = reportGenerators.stream()
            .filter(port -> port.supports(format))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleException(BusinessRuleErrorMessage.UNSUPPORTED_FORMAT.getMessage()));

        byte[] report = generator.generate(results);

        if (report.length == 0)
            throw new ReportGenerationException(ReportGenerationErrorMessage.ERROR_GENERATING_REPORT.getMessage());

        return report;
    }
}
