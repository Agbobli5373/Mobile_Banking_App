package com.mobilebanking.shared.domain;

import com.mobilebanking.shared.domain.exception.InvalidTransactionIdException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionId Value Object Tests")
class TransactionIdTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should generate new TransactionId with random UUID")
        void shouldGenerateNewTransactionIdWithRandomUuid() {
            // When
            TransactionId transactionId1 = TransactionId.generate();
            TransactionId transactionId2 = TransactionId.generate();

            // Then
            assertNotNull(transactionId1);
            assertNotNull(transactionId2);
            assertNotEquals(transactionId1, transactionId2);
            assertNotNull(transactionId1.getValue());
            assertNotNull(transactionId2.getValue());
        }

        @Test
        @DisplayName("Should create TransactionId from valid UUID")
        void shouldCreateTransactionIdFromValidUuid() {
            // Given
            UUID uuid = UUID.randomUUID();

            // When
            TransactionId transactionId = TransactionId.of(uuid);

            // Then
            assertEquals(uuid, transactionId.getValue());
        }

        @Test
        @DisplayName("Should create TransactionId from valid UUID string")
        void shouldCreateTransactionIdFromValidUuidString() {
            // Given
            String uuidString = "123e4567-e89b-12d3-a456-426614174000";

            // When
            TransactionId transactionId = TransactionId.fromString(uuidString);

            // Then
            assertEquals(uuidString, transactionId.asString());
            assertEquals(UUID.fromString(uuidString), transactionId.getValue());
        }

        @Test
        @DisplayName("Should create TransactionId from UUID string with whitespace")
        void shouldCreateTransactionIdFromUuidStringWithWhitespace() {
            // Given
            String uuidString = "  123e4567-e89b-12d3-a456-426614174000  ";
            String expectedUuid = "123e4567-e89b-12d3-a456-426614174000";

            // When
            TransactionId transactionId = TransactionId.fromString(uuidString);

            // Then
            assertEquals(expectedUuid, transactionId.asString());
        }

        @Test
        @DisplayName("Should throw exception for null UUID")
        void shouldThrowExceptionForNullUuid() {
            // When & Then
            InvalidTransactionIdException exception = assertThrows(
                    InvalidTransactionIdException.class,
                    () -> TransactionId.of(null));
            assertEquals("UUID cannot be null", exception.getMessage());
            assertEquals("INVALID_TRANSACTION_ID", exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw exception for null UUID string")
        void shouldThrowExceptionForNullUuidString() {
            // When & Then
            InvalidTransactionIdException exception = assertThrows(
                    InvalidTransactionIdException.class,
                    () -> TransactionId.fromString(null));
            assertEquals("UUID string cannot be null or empty", exception.getMessage());
            assertEquals("INVALID_TRANSACTION_ID", exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw exception for empty UUID string")
        void shouldThrowExceptionForEmptyUuidString() {
            // When & Then
            InvalidTransactionIdException exception = assertThrows(
                    InvalidTransactionIdException.class,
                    () -> TransactionId.fromString(""));
            assertEquals("UUID string cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for whitespace-only UUID string")
        void shouldThrowExceptionForWhitespaceOnlyUuidString() {
            // When & Then
            InvalidTransactionIdException exception = assertThrows(
                    InvalidTransactionIdException.class,
                    () -> TransactionId.fromString("   "));
            assertEquals("UUID string cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid UUID format")
        void shouldThrowExceptionForInvalidUuidFormat() {
            // Given
            String invalidUuid = "invalid-uuid-format";

            // When & Then
            InvalidTransactionIdException exception = assertThrows(
                    InvalidTransactionIdException.class,
                    () -> TransactionId.fromString(invalidUuid));
            assertTrue(exception.getMessage().contains("Invalid UUID format"));
            assertTrue(exception.getMessage().contains(invalidUuid));
            assertNotNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Equality and Hash Tests")
    class EqualityAndHashTests {

        @Test
        @DisplayName("Should be equal when UUIDs are the same")
        void shouldBeEqualWhenUuidsAreTheSame() {
            // Given
            UUID uuid = UUID.randomUUID();
            TransactionId transactionId1 = TransactionId.of(uuid);
            TransactionId transactionId2 = TransactionId.of(uuid);

            // When & Then
            assertEquals(transactionId1, transactionId2);
            assertEquals(transactionId1.hashCode(), transactionId2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when UUIDs are different")
        void shouldNotBeEqualWhenUuidsAreDifferent() {
            // Given
            TransactionId transactionId1 = TransactionId.generate();
            TransactionId transactionId2 = TransactionId.generate();

            // When & Then
            assertNotEquals(transactionId1, transactionId2);
        }

        @Test
        @DisplayName("Should not be equal to null or different type")
        void shouldNotBeEqualToNullOrDifferentType() {
            // Given
            TransactionId transactionId = TransactionId.generate();

            // When & Then
            assertNotEquals(transactionId, null);
            assertNotEquals(transactionId, "some-string");
            assertNotEquals(transactionId, UUID.randomUUID());
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            TransactionId transactionId = TransactionId.generate();

            // When & Then
            assertEquals(transactionId, transactionId);
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should return correct string representation")
        void shouldReturnCorrectStringRepresentation() {
            // Given
            UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            TransactionId transactionId = TransactionId.of(uuid);

            // When
            String result = transactionId.toString();
            String asStringResult = transactionId.asString();

            // Then
            assertEquals("123e4567-e89b-12d3-a456-426614174000", result);
            assertEquals("123e4567-e89b-12d3-a456-426614174000", asStringResult);
            assertEquals(result, asStringResult);
        }
    }

    @Nested
    @DisplayName("Value Access Tests")
    class ValueAccessTests {

        @Test
        @DisplayName("Should return correct UUID value")
        void shouldReturnCorrectUuidValue() {
            // Given
            UUID uuid = UUID.randomUUID();
            TransactionId transactionId = TransactionId.of(uuid);

            // When
            UUID result = transactionId.getValue();

            // Then
            assertEquals(uuid, result);
            assertSame(uuid, result);
        }
    }
}