package com.deathz.laborcalc.application.exceptions;

public class BusinessRuleException extends RuntimeException {
    
    public BusinessRuleException(String message) {
        super(message);
    }
}