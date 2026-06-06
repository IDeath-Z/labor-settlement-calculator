package com.deathz.laborcalc.application.exceptions.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExternalServiceNoDataFoundErrorMessage {

    BACEN_MINIMUM_WAGE_NOT_FOUND("Failed to retrieve minimum wage data from Bacen API "),
    BACEN_SELIC_NOT_FOUND("Failed to retrieve SELIC rate data from Bacen API "),
    BACEN_MINIMUM_WAGE_CONNECTION_ERROR("Failed to connect or parse minimum wage data from Bacen API "),
    BACEN_SELIC_CONNECTION_ERROR("Failed to connect or parse SELIC rate data from Bacen API ");

    private final String message;
}
