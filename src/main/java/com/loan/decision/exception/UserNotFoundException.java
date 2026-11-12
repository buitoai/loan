package com.loan.decision.exception;

/**
 * Exception thrown when a user cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("User not found with id: " + userId);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
