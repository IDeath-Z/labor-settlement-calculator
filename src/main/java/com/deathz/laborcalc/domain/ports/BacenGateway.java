package com.deathz.laborcalc.domain.ports;

import java.time.LocalDate;
import java.util.List;

import com.deathz.laborcalc.domain.model.SelicRate;

public interface BacenGateway {

    List<SelicRate> getSelicRateHistory(LocalDate startDate, LocalDate endDate);
}
