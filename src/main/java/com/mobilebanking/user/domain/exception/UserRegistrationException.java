package com.mobilebanking.user.domain.exception;

/**
 * Exception thrown when user registration fails for general reasons.
 */
public class UserRegistrationException extends RuntimeException {

    public UserRegistrationException(String message) {
        super(message);
    }

    public UserRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}