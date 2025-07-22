package com.mobilebanking.shared.domain.exception;

/**
 * Exception thrown when user name validation fails.
 */
public class InvalidUserNameException extends DomainException {

    public InvalidUserNameException(String message) {
        super("INVALID_USER_NAME", message);
    }

    private InvalidUserNameException(String message, Throwable cause) {
        super("INVALID_USER_NAME", message, cause);
    }

    public static InvalidUserNameException nullOrEmpty() {
        return new InvalidUserNameException("User name cannot be null or empty");
    }

    public static InvalidUserNameException invalidLength(int minLength, int maxLength) {
        return new InvalidUserNameException(
                String.format("User name must be between %d and %d characters", minLength, maxLength));
    }

    public static InvalidUserNameException invalidFormat() {
        return new InvalidUserNameException(
                "User name can only contain letters, spaces, hyphens, and apostrophes");
    }

    public static InvalidUserNameException invalidStartOrEnd() {
        return new InvalidUserNameException(
                "User name cannot start or end with spaces, hyphens, or apostrophes");
    }

    public static InvalidUserNameException consecutiveSpecialCharacters() {
        return new InvalidUserNameException(
                "User name cannot contain consecutive special characters");
    }
}