package com.loan.decision.exception;

/**
 * Exception thrown when a loan application cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class LoanNotFoundException extends RuntimeException {

    public LoanNotFoundException(Long applicationId) {
        super("Loan application not found with id: " + applicationId);
    }

    public LoanNotFoundException(String message) {
        super(message);
    }
}
