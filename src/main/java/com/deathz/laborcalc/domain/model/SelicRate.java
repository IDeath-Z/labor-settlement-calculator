package com.deathz.laborcalc.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SelicRate(
    LocalDate date,
    BigDecimal rateValue
) {}
