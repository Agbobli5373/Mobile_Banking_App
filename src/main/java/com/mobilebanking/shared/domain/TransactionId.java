package com.mobilebanking.shared.domain;

import com.mobilebanking.shared.domain.exception.InvalidTransactionIdException;
import java.util.Objects;
import java.util.UUID;

/**
 * TransactionId value object that wraps UUID for type safety and domain
 * clarity.
 * Provides a strongly-typed identifier for Transaction entities.
 */
public final class TransactionId {
    private final UUID value;

    private TransactionId(UUID value) {
        this.value = value;
    }

    /**
     * Creates a new TransactionId with a random UUID.
     * 
     * @return new TransactionId instance
     */
    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }

    /**
     * Creates a TransactionId from an existing UUID.
     * 
     * @param uuid the UUID value
     * @return TransactionId instance
     * @throws InvalidTransactionIdException if uuid is null
     */
    public static TransactionId of(UUID uuid) {
        if (uuid == null) {
            throw InvalidTransactionIdException.nullUuid();
        }
        return new TransactionId(uuid);
    }

    /**
     * Creates a TransactionId from a string representation of UUID.
     * 
     * @param uuidString the string representation of UUID
     * @return TransactionId instance
     * @throws InvalidTransactionIdException if uuidString is null, empty, or
     *                                       invalid
     *                                       UUID format
     */
    public static TransactionId fromString(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw InvalidTransactionIdException.nullOrEmptyString();
        }
        try {
            return new TransactionId(UUID.fromString(uuidString.trim()));
        } catch (IllegalArgumentException e) {
            throw InvalidTransactionIdException.invalidFormat(uuidString, e);
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
        TransactionId transactionId = (TransactionId) obj;
        return Objects.equals(value, transactionId.value);
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