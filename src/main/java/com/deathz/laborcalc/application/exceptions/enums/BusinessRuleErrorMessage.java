package com.deathz.laborcalc.application.exceptions.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessRuleErrorMessage {

    SETTLEMENT_EMPTY("Settlement cannot be empty"),
    UNSUPPORTED_FORMAT("Unsupported report format");

    private final String message;
}
