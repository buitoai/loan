package com.loan.decision.exception;

/**
 * Exception thrown when an error occurs during credit scoring computation.
 * Maps to HTTP 500 Internal Server Error.
 */
public class CreditScoringException extends RuntimeException {

    public CreditScoringException(String message) {
        super(message);
    }

    public CreditScoringException(String message, Throwable cause) {
        super(message, cause);
    }
}
