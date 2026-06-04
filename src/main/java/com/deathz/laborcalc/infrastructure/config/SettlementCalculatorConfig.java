package com.deathz.laborcalc.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.deathz.laborcalc.domain.service.SettlementCalculatorService;

@Configuration
public class SettlementCalculatorConfig {

    @Bean
    SettlementCalculatorService settlementCalculatorService() {
        return new SettlementCalculatorService();
    }
}
