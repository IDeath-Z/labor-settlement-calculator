package com.deathz.laborcalc.domain.ports;

import java.time.LocalDate;
import java.util.List;

import com.deathz.laborcalc.domain.model.MinimumWage;

public interface MinimumWageGateway {

    List<MinimumWage> getMinimumWageHistory(LocalDate startDate, LocalDate endDate);
}
