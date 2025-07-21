package com.mobilebanking.shared.domain.exception;

/**
 * Exception thrown when TransactionId value object validation fails.
 */
public class InvalidTransactionIdException extends DomainException {
    private static final String ERROR_CODE = "INVALID_TRANSACTION_ID";

    public InvalidTransactionIdException(String message) {
        super(ERROR_CODE, message);
    }

    public InvalidTransactionIdException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    public static InvalidTransactionIdException nullUuid() {
        return new InvalidTransactionIdException("UUID cannot be null");
    }

    public static InvalidTransactionIdException nullOrEmptyString() {
        return new InvalidTransactionIdException("UUID string cannot be null or empty");
    }

    public static InvalidTransactionIdException invalidFormat(String uuidString, Throwable cause) {
        return new InvalidTransactionIdException("Invalid UUID format: " + uuidString, cause);
    }
}