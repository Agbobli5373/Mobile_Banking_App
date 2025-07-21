package com.mobilebanking.shared.domain;

import com.mobilebanking.shared.domain.exception.InvalidUserIdException;
import java.util.Objects;
import java.util.UUID;

/**
 * UserId value object that wraps UUID for type safety and domain clarity.
 * Provides a strongly-typed identifier for User entities.
 */
public final class UserId {
    private final UUID value;

    private UserId(UUID value) {
        this.value = value;
    }

    /**
     * Creates a new UserId with a random UUID.
     * 
     * @return new UserId instance
     */
    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    /**
     * Creates a UserId from an existing UUID.
     * 
     * @param uuid the UUID value
     * @return UserId instance
     * @throws InvalidUserIdException if uuid is null
     */
    public static UserId of(UUID uuid) {
        if (uuid == null) {
            throw InvalidUserIdException.nullUuid();
        }
        return new UserId(uuid);
    }

    /**
     * Creates a UserId from a string representation of UUID.
     * 
     * @param uuidString the string representation of UUID
     * @return UserId instance
     * @throws InvalidUserIdException if uuidString is null, empty, or invalid
     *                                UUID format
     */
    public static UserId fromString(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw InvalidUserIdException.nullOrEmptyString();
        }
        try {
            return new UserId(UUID.fromString(uuidString.trim()));
        } catch (IllegalArgumentException e) {
            throw InvalidUserIdException.invalidFormat(uuidString, e);
        }
    }

    /**
     * Gets the UUID value.
     * 
     * @return the wrapped UUID
     */
    public UUID getValue() {
        return value;
    }

    /**
     * Gets the string representation of the UUID.
     * 
     * @return string representation of UUID
     */
    public String asString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        UserId userId = (UserId) obj;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}