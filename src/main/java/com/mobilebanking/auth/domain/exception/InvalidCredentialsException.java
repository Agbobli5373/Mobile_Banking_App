package com.mobilebanking.auth.domain.exception;

/**
 * Exception thrown when authentication credentials are invalid.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials provided");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception for invalid phone number or PIN.
     *
     * @return InvalidCredentialsException
     */
    public static InvalidCredentialsException invalidPhoneOrPin() {
        return new InvalidCredentialsException("Invalid phone number or PIN");
    }

    /**
     * Creates an exception for user not found.
     *
     * @return InvalidCredentialsException
     */
    public static InvalidCredentialsException userNotFound() {
        return new InvalidCredentialsException("User not found");
    }
}