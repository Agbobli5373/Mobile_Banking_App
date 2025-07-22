package com.mobilebanking.shared.domain.exception;

import com.mobilebanking.shared.domain.PhoneNumber;

/**
 * Exception thrown when attempting to register a user with a phone number
 * that already exists in the system.
 */
public class DuplicatePhoneNumberException extends RuntimeException {

    private final String phoneNumber;

    public DuplicatePhoneNumberException(String message, String phoneNumber) {
        super(message);
        this.phoneNumber = phoneNumber;
    }

    /**
     * Creates an exception for a duplicate phone number.
     *
     * @param phoneNumber the duplicate phone number
     * @return the exception
     */
    public static DuplicatePhoneNumberException forPhoneNumber(String phoneNumber) {
        return new DuplicatePhoneNumberException(
                String.format("User with phone number '%s' already exists", phoneNumber),
                phoneNumber);
    }

    /**
     * Creates an exception for a duplicate phone number.
     *
     * @param phoneNumber the duplicate phone number value object
     * @return the exception
     */
    public static DuplicatePhoneNumberException forPhoneNumber(PhoneNumber phoneNumber) {
        return forPhoneNumber(phoneNumber.getValue());
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