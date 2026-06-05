package com.deathz.laborcalc.infrastructure.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.deathz.laborcalc.application.usecases.GenerateReportUseCase;
import com.deathz.laborcalc.domain.ports.ReportGeneratorPort;

@Configuration
public class GenerateReportConfig {

    @Bean
    public GenerateReportUseCase generateReportUseCase(List<ReportGeneratorPort> reportGenerators) {
        return new GenerateReportUseCase(reportGenerators);
    }
}
