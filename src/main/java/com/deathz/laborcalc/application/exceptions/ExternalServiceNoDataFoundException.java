package com.deathz.laborcalc.application.exceptions;

public class ExternalServiceNoDataFoundException extends RuntimeException {

    public ExternalServiceNoDataFoundException(String message) {
        super(message);
    }

    public ExternalServiceNoDataFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
