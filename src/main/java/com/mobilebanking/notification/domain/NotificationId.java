package com.mobilebanking.notification.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * NotificationId value object that wraps UUID for type safety and domain clarity.
 * Provides a strongly-typed identifier for Notification entities.
 */
public final class NotificationId {
    private final UUID value;

    private NotificationId(UUID value) {
        this.value = Objects.requireNonNull(value, "NotificationId value cannot be null");
    }

    /**
     * Creates a new NotificationId with a random UUID.
     */
    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }

    /**
     * Creates a NotificationId from an existing UUID.
     */
    public static NotificationId of(UUID uuid) {
        return new NotificationId(uuid);
    }

    /**
     * Creates a NotificationId from a string representation of UUID.
     */
    public static NotificationId fromString(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("NotificationId string cannot be null or empty");
        }
        try {
            return new NotificationId(UUID.fromString(uuidString.trim()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid NotificationId format: " + uuidString, e);
        }
    }

    public UUID getValue() {
        return value;
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotificationId that = (NotificationId) obj;
        return Objects.equals(value, that.value);
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