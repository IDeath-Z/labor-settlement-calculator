package com.deathz.laborcalc.application.usecases;

import java.util.List;

import com.deathz.laborcalc.domain.model.MinimumWage;
import com.deathz.laborcalc.domain.model.SelicRate;
import com.deathz.laborcalc.domain.model.SettlementInput;
import com.deathz.laborcalc.domain.model.SettlementResult;
import com.deathz.laborcalc.domain.ports.MinimumWageGateway;
import com.deathz.laborcalc.domain.ports.SelicGateway;
import com.deathz.laborcalc.domain.service.SettlementCalculatorService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CalculateLaborSettlementUseCase {

    private final MinimumWageGateway minimumWageGateway;
    private final SelicGateway selicGateway;
    private final SettlementCalculatorService settlementCalculatorService;

    public List<SettlementResult> execute(SettlementInput input) {

        List<MinimumWage> minimumWages = minimumWageGateway.getMinimumWageHistory(input.startDate(), input.endDate());

        System.out.println("Minimum wages retrieved: " + minimumWages.size());
        List<SelicRate> selicRates = selicGateway.getSelicRateHistory(input.startDate(), input.endDate());

        System.out.println("Selic rates retrieved: " + selicRates.size());
        
        return settlementCalculatorService.calculate(input, minimumWages, selicRates);
    }
}
