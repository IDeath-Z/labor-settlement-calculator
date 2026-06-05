package com.deathz.laborcalc.domain.ports;

import java.util.List;

import com.deathz.laborcalc.domain.enums.ReportFormat;
import com.deathz.laborcalc.domain.model.SettlementResult;

public interface ReportGeneratorPort {

    boolean supports(ReportFormat format);
    byte[] generate(List<SettlementResult> results);
}
