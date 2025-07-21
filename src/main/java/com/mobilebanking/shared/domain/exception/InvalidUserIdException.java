package com.mobilebanking.shared.domain.exception;

/**
 * Exception thrown when UserId value object validation fails.
 */
public class InvalidUserIdException extends DomainException {
    private static final String ERROR_CODE = "INVALID_USER_ID";

    public InvalidUserIdException(String message) {
        super(ERROR_CODE, message);
    }

    public InvalidUserIdException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    public static InvalidUserIdException nullUuid() {
        return new InvalidUserIdException("UUID cannot be null");
    }

    public static InvalidUserIdException nullOrEmptyString() {
        return new InvalidUserIdException("UUID string cannot be null or empty");
    }

    public static InvalidUserIdException invalidFormat(String uuidString, Throwable cause) {
        return new InvalidUserIdException("Invalid UUID format: " + uuidString, cause);
    }
}