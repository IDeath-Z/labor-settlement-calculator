package com.deathz.laborcalc.application.usecases;

import java.util.List;

import com.deathz.laborcalc.application.exceptions.ExternalServiceNoDataFoundException;
import com.deathz.laborcalc.application.exceptions.enums.ExternalServiceNoDataFoundErrorMessage;
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

        if (minimumWages == null || minimumWages.isEmpty())
            throw new ExternalServiceNoDataFoundException(ExternalServiceNoDataFoundErrorMessage.BACEN_MINIMUM_WAGE_NOT_FOUND.getMessage());
        
        List<SelicRate> selicRates = selicGateway.getSelicRateHistory(input.startDate(), input.endDate());

        if (selicRates == null || selicRates.isEmpty())
            throw new ExternalServiceNoDataFoundException(ExternalServiceNoDataFoundErrorMessage.BACEN_SELIC_NOT_FOUND.getMessage());

        return settlementCalculatorService.calculate(input, minimumWages, selicRates);
    }
}
