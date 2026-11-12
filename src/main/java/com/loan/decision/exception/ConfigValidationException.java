package com.loan.decision.exception;

/**
 * Exception thrown when system configuration validation fails.
 * Maps to HTTP 400 Bad Request.
 */
public class ConfigValidationException extends RuntimeException {

    public ConfigValidationException(String message) {
        super(message);
    }
}
