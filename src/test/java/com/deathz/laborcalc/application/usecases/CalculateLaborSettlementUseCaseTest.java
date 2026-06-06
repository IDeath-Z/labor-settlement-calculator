package com.deathz.laborcalc.application.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.deathz.laborcalc.application.exceptions.ExternalServiceNoDataFoundException;
import com.deathz.laborcalc.domain.model.MinimumWage;
import com.deathz.laborcalc.domain.model.SettlementInput;
import com.deathz.laborcalc.domain.ports.MinimumWageGateway;
import com.deathz.laborcalc.domain.ports.SelicGateway;
import com.deathz.laborcalc.domain.service.SettlementCalculatorService;
import com.deathz.laborcalc.fixtures.SettlementInputFixture;

@ExtendWith(MockitoExtension.class)
class CalculateLaborSettlementUseCaseTest {

    @Mock
    private MinimumWageGateway minimumWageGateway;

    @Mock
    private SelicGateway selicGateway;

    @Mock
    private SettlementCalculatorService settlementCalculatorService;

    @InjectMocks
    private CalculateLaborSettlementUseCase useCase;

    @Test
    @DisplayName("Should throw when wage history is empty")
    void shouldThrowWhenWageHistoryIsEmpty() {
        SettlementInput input = SettlementInputFixture.createWithoutPandemicOrShiftRotation();
        when(minimumWageGateway.getMinimumWageHistory(any(), any())).thenReturn(List.of());

        assertThrows(ExternalServiceNoDataFoundException.class, () -> useCase.execute(input));
    }

    @Test
    @DisplayName("Should throw when selic history is empty")
    void shouldThrowWhenSelicHistoryIsEmpty() {
        SettlementInput input = SettlementInputFixture.createWithoutPandemicOrShiftRotation();
        when(minimumWageGateway.getMinimumWageHistory(any(), any())).thenReturn(List.of(new MinimumWage(input.startDate(), BigDecimal.valueOf(1500))));
        when(selicGateway.getSelicRateHistory(any(), any())).thenReturn(List.of());

        assertThrows(ExternalServiceNoDataFoundException.class, () -> useCase.execute(input));
    }
}
