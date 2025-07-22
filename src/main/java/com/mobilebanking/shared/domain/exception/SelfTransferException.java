package com.mobilebanking.shared.domain.exception;

/**
 * Exception thrown when a user attempts to transfer money to themselves.
 */
public class SelfTransferException extends DomainException {
    private static final String ERROR_CODE = "SELF_TRANSFER_ERROR";

    public SelfTransferException() {
        super( ERROR_CODE ,"Cannot transfer money to yourself");
    }

    public SelfTransferException(String message) {
        super(ERROR_CODE, message);
    }

    public SelfTransferException(String message, Throwable cause) {
        super(message, cause.getMessage());
    }
}