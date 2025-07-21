package com.mobilebanking.shared.domain.exception;

/**
 * Exception thrown when PIN validation fails.
 */
public class InvalidPinException extends DomainException {

    private InvalidPinException(String message) {
        super("INVALID_PIN", message);
    }

    private InvalidPinException(String message, Throwable cause) {
        super("INVALID_PIN", message, cause);
    }

    public static InvalidPinException nullOrEmptyRawPin() {
        return new InvalidPinException("PIN cannot be null or empty");
    }

    public static InvalidPinException nullOrEmptyHash() {
        return new InvalidPinException("Hashed PIN cannot be null or empty");
    }

    public static InvalidPinException invalidLength(int minLength, int maxLength) {
        return new InvalidPinException(
                String.format("PIN must be between %d and %d digits", minLength, maxLength));
    }

    public static InvalidPinException invalidFormat() {
        return new InvalidPinException("PIN must contain only numeric digits");
    }
}