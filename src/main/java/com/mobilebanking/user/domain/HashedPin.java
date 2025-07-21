package com.mobilebanking.user.domain;

import com.mobilebanking.shared.domain.exception.InvalidPinException;
import jakarta.persistence.Embeddable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Objects;

/**
 * HashedPin value object that handles PIN hashing and validation using BCrypt.
 * Ensures PINs are securely stored and validated.
 */
@Embeddable
public final class HashedPin {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final int MIN_PIN_LENGTH = 4;
    private static final int MAX_PIN_LENGTH = 6;

    private final String hashedValue;

    // JPA requires default constructor
    public HashedPin() {
        this.hashedValue = "";
    }

    private HashedPin(String hashedValue) {
        this.hashedValue = hashedValue;
    }

    /**
     * Creates a HashedPin from a raw PIN string.
     * 
     * @param rawPin the raw PIN to hash
     * @return HashedPin instance
     * @throws InvalidPinException if PIN is invalid
     */
    public static HashedPin fromRawPin(String rawPin) {
        validateRawPin(rawPin);
        String trimmedPin = rawPin.trim();
        String hashed = encoder.encode(trimmedPin);
        return new HashedPin(hashed);
    }

    /**
     * Creates a HashedPin from an already hashed value.
     * Used when loading from database.
     * 
     * @param hashedValue the already hashed PIN
     * @return HashedPin instance
     * @throws InvalidPinException if hashedValue is null or empty
     */
    public static HashedPin fromHashedValue(String hashedValue) {
        if (hashedValue == null || hashedValue.trim().isEmpty()) {
            throw InvalidPinException.nullOrEmptyHash();
        }
        return new HashedPin(hashedValue);
    }

    /**
     * Validates a raw PIN against this hashed PIN.
     * 
     * @param rawPin the raw PIN to validate
     * @return true if PIN matches, false otherwise
     * @throws InvalidPinException if rawPin is null or empty
     */
    public boolean matches(String rawPin) {
        if (rawPin == null || rawPin.trim().isEmpty()) {
            throw InvalidPinException.nullOrEmptyRawPin();
        }
        // Trim the PIN for comparison since we trim during creation
        return encoder.matches(rawPin.trim(), hashedValue);
    }

    /**
     * Gets the hashed PIN value for storage.
     * 
     * @return the hashed PIN string
     */
    public String getHashedValue() {
        return hashedValue;
    }

    /**
     * Validates the raw PIN format and requirements.
     * 
     * @param rawPin the raw PIN to validate
     * @throws InvalidPinException if PIN is invalid
     */
    private static void validateRawPin(String rawPin) {
        if (rawPin == null) {
            throw InvalidPinException.nullOrEmptyRawPin();
        }

        String trimmedPin = rawPin.trim();

        if (trimmedPin.isEmpty()) {
            throw InvalidPinException.nullOrEmptyRawPin();
        }

        if (trimmedPin.length() < MIN_PIN_LENGTH || trimmedPin.length() > MAX_PIN_LENGTH) {
            throw InvalidPinException.invalidLength(MIN_PIN_LENGTH, MAX_PIN_LENGTH);
        }

        if (!trimmedPin.matches("\\d+")) {
            throw InvalidPinException.invalidFormat();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        HashedPin hashedPin = (HashedPin) obj;
        return Objects.equals(hashedValue, hashedPin.hashedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedValue);
    }

    @Override
    public String toString() {
        return "[PROTECTED]"; // Never expose the actual hash
    }
}