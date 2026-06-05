package com.deathz.laborcalc.application.usecases;

import java.util.List;

import com.deathz.laborcalc.domain.enums.ReportFormat;
import com.deathz.laborcalc.domain.model.SettlementResult;
import com.deathz.laborcalc.domain.ports.ReportGeneratorPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenerateReportUseCase {

    private final List<ReportGeneratorPort> reportGenerators;

    public byte[] execute(List<SettlementResult> results, ReportFormat format) {
        if (results == null || results.isEmpty())
            throw new IllegalArgumentException("No settlement results to generate report.");

        ReportGeneratorPort generator = reportGenerators.stream()
            .filter(port -> port.supports(format))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported format: " + format));

        return generator.generate(results);
    }
}
