package com.deathz.laborcalc.presentation.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.deathz.laborcalc.application.exceptions.BusinessRuleException;
import com.deathz.laborcalc.application.exceptions.ExternalServiceNoDataFoundException;
import com.deathz.laborcalc.application.exceptions.ReportGenerationException;
import com.deathz.laborcalc.presentation.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String GENERIC_ERROR_MESSAGE = "An unexpected error occurred. Please try again later.";

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRuleException(BusinessRuleException e, HttpServletRequest request) {
        log.info("Business rule violation: {}", e.getMessage());
        
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiErrorResponse error = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(), 
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ExternalServiceNoDataFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBadGateway(ExternalServiceNoDataFoundException e, HttpServletRequest request) {
        log.warn("External service error: {}", e.getMessage());
        
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        ApiErrorResponse error = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ReportGenerationException.class)
    public ResponseEntity<ApiErrorResponse> handleReportGenerationError(ReportGenerationException e, HttpServletRequest request) {
        log.error("Report generation error: {}", e.getMessage(), e);
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiErrorResponse error = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(), 
            GENERIC_ERROR_MESSAGE,
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUncaughtException(Exception e, HttpServletRequest request) {
        log.error("Internal server error: {}", e.getMessage(), e);
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiErrorResponse error = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            GENERIC_ERROR_MESSAGE,
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }
}