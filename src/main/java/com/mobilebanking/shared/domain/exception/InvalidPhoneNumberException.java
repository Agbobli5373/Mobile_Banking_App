package com.mobilebanking.shared.domain.exception;

/**
 * Exception thrown when PhoneNumber value object validation fails.
 */
public class InvalidPhoneNumberException extends DomainException {
    private static final String ERROR_CODE = "INVALID_PHONE_NUMBER";

    public InvalidPhoneNumberException(String message) {
        super(ERROR_CODE, message);
    }

    public InvalidPhoneNumberException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    public static InvalidPhoneNumberException nullOrEmpty() {
        return new InvalidPhoneNumberException("Phone number cannot be null or empty");
    }

    public static InvalidPhoneNumberException invalidLength(int minLength, int maxLength) {
        return new InvalidPhoneNumberException(
                String.format("Phone number must be between %d and %d digits", minLength, maxLength));
    }

    public static InvalidPhoneNumberException invalidFormat() {
        return new InvalidPhoneNumberException("Invalid phone number format");
    }
}