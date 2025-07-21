package com.mobilebanking.shared.domain;

import com.mobilebanking.shared.domain.exception.InvalidPhoneNumberException;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * PhoneNumber value object that handles phone number validation and formatting.
 * Ensures phone numbers meet the required format and business rules.
 */
@Embeddable
public final class PhoneNumber {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final int MIN_LENGTH = 7;
    private static final int MAX_LENGTH = 15;

    private final String value;

    // JPA requires default constructor
    public PhoneNumber() {
        this.value = "";
    }

    private PhoneNumber(String value) {
        this.value = value;
    }

    /**
     * Creates a PhoneNumber from a string value.
     * 
     * @param phoneNumber the phone number string
     * @return PhoneNumber instance
     * @throws InvalidPhoneNumberException if phone number is invalid
     */
    public static PhoneNumber of(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw InvalidPhoneNumberException.nullOrEmpty();
        }

        String normalized = normalize(phoneNumber);
        validateFormat(normalized);

        return new PhoneNumber(normalized);
    }

    /**
     * Normalizes the phone number by removing spaces, dashes, and parentheses.
     * 
     * @param phoneNumber the raw phone number
     * @return normalized phone number
     */
    private static String normalize(String phoneNumber) {
        return phoneNumber.trim()
                .replaceAll("[\\s\\-\\(\\)]", "");
    }

    /**
     * Validates the phone number format and length.
     * 
     * @param phoneNumber the normalized phone number
     * @throws InvalidPhoneNumberException if format is invalid
     */
    private static void validateFormat(String phoneNumber) {
        // Count only digits for length validation (exclude + sign)
        String digitsOnly = phoneNumber.startsWith("+") ? phoneNumber.substring(1) : phoneNumber;

        if (digitsOnly.length() < MIN_LENGTH || digitsOnly.length() > MAX_LENGTH) {
            throw InvalidPhoneNumberException.invalidLength(MIN_LENGTH, MAX_LENGTH);
        }

        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw InvalidPhoneNumberException.invalidFormat();
        }
    }

    /**
     * Gets the phone number value.
     * 
     * @return the phone number string
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets a formatted display version of the phone number.
     * 
     * @return formatted phone number for display
     */
    public String getDisplayValue() {
        if (value.startsWith("+")) {
            return value;
        }
        return "+" + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        PhoneNumber that = (PhoneNumber) obj;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}