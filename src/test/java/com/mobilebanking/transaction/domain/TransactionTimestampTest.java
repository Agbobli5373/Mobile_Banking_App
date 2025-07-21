package com.mobilebanking.transaction.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionTimestamp Value Object Tests")
class TransactionTimestampTest {

    @Test
    @DisplayName("Should create timestamp with current time")
    void shouldCreateTimestampWithCurrentTime() {
        // Given
        Instant before = Instant.now();
        
        // When
        TransactionTimestamp timestamp = TransactionTimestamp.now();
        
        // Then
        assertNotNull(timestamp);
        assertNotNull(timestamp.getValue());
        assertTrue(timestamp.getValue().isAfter(before.minusSeconds(1)));
        assertTrue(timestamp.getValue().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Should create timestamp from existing Instant")
    void shouldCreateTimestampFromExistingInstant() {
        // Given
        Instant instant = Instant.parse("2023-01-01T12:00:00Z");
        
        // When
        TransactionTimestamp timestamp = TransactionTimestamp.of(instant);
        
        // Then
        assertNotNull(timestamp);
        assertEquals(instant, timestamp.getValue());
    }

    @Test
    @DisplayName("Should throw exception when creating timestamp with null Instant")
    void shouldThrowExceptionWhenCreatingTimestampWithNullInstant() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TransactionTimestamp.of(null)
        );
        assertEquals("Transaction timestamp cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should correctly compare timestamps - isBefore")
    void shouldCorrectlyCompareTimestampsIsBefore() {
        // Given
        Instant earlier = Instant.parse("2023-01-01T12:00:00Z");
        Instant later = Instant.parse("2023-01-01T13:00:00Z");
        TransactionTimestamp earlierTimestamp = TransactionTimestamp.of(earlier);
        TransactionTimestamp laterTimestamp = TransactionTimestamp.of(later);
        
        // When & Then
        assertTrue(earlierTimestamp.isBefore(laterTimestamp));
        assertFalse(laterTimestamp.isBefore(earlierTimestamp));
        assertFalse(earlierTimestamp.isBefore(earlierTimestamp)); // Same timestamp
    }

    @Test
    @DisplayName("Should correctly compare timestamps - isAfter")
    void shouldCorrectlyCompareTimestampsIsAfter() {
        // Given
        Instant earlier = Instant.parse("2023-01-01T12:00:00Z");
        Instant later = Instant.parse("2023-01-01T13:00:00Z");
        TransactionTimestamp earlierTimestamp = TransactionTimestamp.of(earlier);
        TransactionTimestamp laterTimestamp = TransactionTimestamp.of(later);
        
        // When & Then
        assertTrue(laterTimestamp.isAfter(earlierTimestamp));
        assertFalse(earlierTimestamp.isAfter(laterTimestamp));
        assertFalse(earlierTimestamp.isAfter(earlierTimestamp)); // Same timestamp
    }

    @Test
    @DisplayName("Should throw exception when comparing with null timestamp")
    void shouldThrowExceptionWhenComparingWithNullTimestamp() {
        // Given
        TransactionTimestamp timestamp = TransactionTimestamp.now();
        
        // When & Then
        IllegalArgumentException beforeException = assertThrows(
            IllegalArgumentException.class,
            () -> timestamp.isBefore(null)
        );
        assertEquals("Cannot compare with null timestamp", beforeException.getMessage());

        IllegalArgumentException afterException = assertThrows(
            IllegalArgumentException.class,
            () -> timestamp.isAfter(null)
        );
        assertEquals("Cannot compare with null timestamp", afterException.getMessage());
    }

    @Test
    @DisplayName("Should have proper equality based on Instant value")
    void shouldHaveProperEqualityBasedOnInstantValue() {
        // Given
        Instant instant = Instant.parse("2023-01-01T12:00:00Z");
        TransactionTimestamp timestamp1 = TransactionTimestamp.of(instant);
        TransactionTimestamp timestamp2 = TransactionTimestamp.of(instant);
        TransactionTimestamp differentTimestamp = TransactionTimestamp.now();
        
        // When & Then
        assertEquals(timestamp1, timestamp2);
        assertEquals(timestamp1, timestamp1);
        assertNotEquals(timestamp1, differentTimestamp);
        assertNotEquals(timestamp1, null);
        assertNotEquals(timestamp1, "not a timestamp");
    }

    @Test
    @DisplayName("Should have consistent hashCode based on Instant value")
    void shouldHaveConsistentHashCodeBasedOnInstantValue() {
        // Given
        Instant instant = Instant.parse("2023-01-01T12:00:00Z");
        TransactionTimestamp timestamp1 = TransactionTimestamp.of(instant);
        TransactionTimestamp timestamp2 = TransactionTimestamp.of(instant);
        
        // When & Then
        assertEquals(timestamp1.hashCode(), timestamp2.hashCode());
        assertEquals(timestamp1.hashCode(), timestamp1.hashCode());
    }

    @Test
    @DisplayName("Should have meaningful toString representation")
    void shouldHaveMeaningfulToStringRepresentation() {
        // Given
        Instant instant = Instant.parse("2023-01-01T12:00:00Z");
        TransactionTimestamp timestamp = TransactionTimestamp.of(instant);
        
        // When
        String toString = timestamp.toString();
        
        // Then
        assertNotNull(toString);
        assertEquals(instant.toString(), toString);
    }
}