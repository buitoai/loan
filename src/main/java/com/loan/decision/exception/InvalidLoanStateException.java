package com.loan.decision.exception;

/**
 * Exception thrown when a loan application is in an invalid state for the requested operation.
 * Maps to HTTP 409 Conflict.
 */
public class InvalidLoanStateException extends RuntimeException {

    public InvalidLoanStateException(String message) {
        super(message);
    }

    public InvalidLoanStateException(String currentStatus, String requiredStatus) {
        super("Invalid loan state: current=" + currentStatus + ", required=" + requiredStatus);
    }
}
