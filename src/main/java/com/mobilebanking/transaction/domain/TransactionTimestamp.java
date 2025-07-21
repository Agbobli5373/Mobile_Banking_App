package com.mobilebanking.transaction.domain;

import jakarta.persistence.Embeddable;
import java.time.Instant;
import java.util.Objects;

/**
 * TransactionTimestamp value object that encapsulates transaction timing
 * with proper validation and immutability.
 */
@Embeddable
public final class TransactionTimestamp {
    private final Instant timestamp;

    // JPA requires default constructor
    public TransactionTimestamp() {
        this.timestamp = Instant.now();
    }

    private TransactionTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Creates a TransactionTimestamp with the current time.
     * 
     * @return TransactionTimestamp instance with current time
     */
    public static TransactionTimestamp now() {
        return new TransactionTimestamp(Instant.now());
    }

    /**
     * Creates a TransactionTimestamp from an existing Instant.
     * 
     * @param timestamp the Instant value
     * @return TransactionTimestamp instance
     * @throws IllegalArgumentException if timestamp is null
     */
    public static TransactionTimestamp of(Instant timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Transaction timestamp cannot be null");
        }
        return new TransactionTimestamp(timestamp);
    }

    /**
     * Gets the Instant value of this timestamp.
     * 
     * @return the wrapped Instant
     */
    public Instant getValue() {
        return timestamp;
    }

    /**
     * Checks if this timestamp is before another timestamp.
     * 
     * @param other the other TransactionTimestamp to compare with
     * @return true if this timestamp is before the other
     * @throws IllegalArgumentException if other is null
     */
    public boolean isBefore(TransactionTimestamp other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare with null timestamp");
        }
        return this.timestamp.isBefore(other.timestamp);
    }

    /**
     * Checks if this timestamp is after another timestamp.
     * 
     * @param other the other TransactionTimestamp to compare with
     * @return true if this timestamp is after the other
     * @throws IllegalArgumentException if other is null
     */
    public boolean isAfter(TransactionTimestamp other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare with null timestamp");
        }
        return this.timestamp.isAfter(other.timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        TransactionTimestamp that = (TransactionTimestamp) obj;
        return Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp);
    }

    @Override
    public String toString() {
        return timestamp.toString();
    }
}