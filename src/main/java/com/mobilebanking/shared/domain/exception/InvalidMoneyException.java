package com.mobilebanking.shared.domain.exception;

/**
 * Exception thrown when Money value object validation fails.
 */
public class InvalidMoneyException extends DomainException {
    private static final String ERROR_CODE = "INVALID_MONEY";

    public InvalidMoneyException(String message) {
        super(ERROR_CODE, message);
    }

    public InvalidMoneyException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    public static InvalidMoneyException nullAmount() {
        return new InvalidMoneyException("Amount cannot be null");
    }

    public static InvalidMoneyException negativeAmount() {
        return new InvalidMoneyException("Amount cannot be negative");
    }

    public static InvalidMoneyException nullMoneyOperation(String operation) {
        return new InvalidMoneyException("Cannot " + operation + " null Money");
    }

    public static InvalidMoneyException negativeResult() {
        return new InvalidMoneyException("Operation result cannot be negative");
    }
}