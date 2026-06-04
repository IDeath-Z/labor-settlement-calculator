package com.deathz.laborcalc.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.deathz.laborcalc.application.usecases.CalculateLaborSettlementUseCase;
import com.deathz.laborcalc.domain.ports.MinimumWageGateway;
import com.deathz.laborcalc.domain.ports.SelicGateway;
import com.deathz.laborcalc.domain.service.SettlementCalculatorService;

@Configuration
public class CalculateLaborSettlementConfig {

    @Bean
    public CalculateLaborSettlementUseCase calculateLaborSettlementUseCase(MinimumWageGateway minimumWageGateway, SelicGateway selicGateway, 
        SettlementCalculatorService settlementCalculatorService) {
        return new CalculateLaborSettlementUseCase(minimumWageGateway, selicGateway, settlementCalculatorService);
    }
}
