package com.deathz.laborcalc.presentation.mapper;

import org.springframework.stereotype.Component;

import com.deathz.laborcalc.domain.model.SettlementInput;
import com.deathz.laborcalc.presentation.dto.SettlementInputRequest;

@Component
public class SettlementInputMapper {

    public SettlementInput toDomain(SettlementInputRequest request) {
        return new SettlementInput(
            request.startDate(),
            request.endDate(),
            request.pandemicStartDate(),
            request.pandemicEndDate(),
            request.shiftRotationStart(),
            request.shiftRotationInterval(),
            request.additionalPercentage()
        );
    }
}
