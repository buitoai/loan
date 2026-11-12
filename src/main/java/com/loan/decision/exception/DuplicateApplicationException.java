package com.loan.decision.exception;

/**
 * Exception thrown when a duplicate loan application is detected for the same ID number.
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException(String idNumber) {
        super("An active application already exists for ID number: " + idNumber);
    }
}
