package com.deathz.laborcalc.domain.exceptions;

import java.time.YearMonth;

public class IncompleteSeriesException extends RuntimeException {

    public IncompleteSeriesException(YearMonth missingMonth) {
        super("Bacen Series missing data for month: " + missingMonth);
    }
}