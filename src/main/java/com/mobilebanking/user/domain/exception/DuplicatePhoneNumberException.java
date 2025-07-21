package com.mobilebanking.user.domain.exception;

/**
 * Exception thrown when attempting to register a user with an already
 * registered phone number.
 */
public class DuplicatePhoneNumberException extends RuntimeException {

    private final String phoneNumber;

    private DuplicatePhoneNumberException(String message, String phoneNumber) {
        super(message);
        this.phoneNumber = phoneNumber;
    }

    /**
     * Creates a DuplicatePhoneNumberException for the specified phone number.
     *
     * @param phoneNumber the duplicate phone number
     * @return DuplicatePhoneNumberException instance
     */
    public static DuplicatePhoneNumberException forPhoneNumber(String phoneNumber) {
        return new DuplicatePhoneNumberException(
                "Phone number is already registered: " + phoneNumber,
                phoneNumber);
    }

    /**
     * Gets the duplicate phone number.
     *
     * @return the phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
}