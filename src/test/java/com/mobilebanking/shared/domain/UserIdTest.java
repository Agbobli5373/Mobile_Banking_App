package com.mobilebanking.shared.domain;

import com.mobilebanking.shared.domain.exception.InvalidUserIdException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserId Value Object Tests")
class UserIdTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should generate new UserId with random UUID")
        void shouldGenerateNewUserIdWithRandomUuid() {
            // When
            UserId userId1 = UserId.generate();
            UserId userId2 = UserId.generate();

            // Then
            assertNotNull(userId1);
            assertNotNull(userId2);
            assertNotEquals(userId1, userId2);
            assertNotNull(userId1.getValue());
            assertNotNull(userId2.getValue());
        }

        @Test
        @DisplayName("Should create UserId from valid UUID")
        void shouldCreateUserIdFromValidUuid() {
            // Given
            UUID uuid = UUID.randomUUID();

            // When
            UserId userId = UserId.of(uuid);

            // Then
            assertEquals(uuid, userId.getValue());
        }

        @Test
        @DisplayName("Should create UserId from valid UUID string")
        void shouldCreateUserIdFromValidUuidString() {
            // Given
            String uuidString = "123e4567-e89b-12d3-a456-426614174000";

            // When
            UserId userId = UserId.fromString(uuidString);

            // Then
            assertEquals(uuidString, userId.asString());
            assertEquals(UUID.fromString(uuidString), userId.getValue());
        }

        @Test
        @DisplayName("Should create UserId from UUID string with whitespace")
        void shouldCreateUserIdFromUuidStringWithWhitespace() {
            // Given
            String uuidString = "  123e4567-e89b-12d3-a456-426614174000  ";
            String expectedUuid = "123e4567-e89b-12d3-a456-426614174000";

            // When
            UserId userId = UserId.fromString(uuidString);

            // Then
            assertEquals(expectedUuid, userId.asString());
        }

        @Test
        @DisplayName("Should throw exception for null UUID")
        void shouldThrowExceptionForNullUuid() {
            // When & Then
            InvalidUserIdException exception = assertThrows(
                    InvalidUserIdException.class,
                    () -> UserId.of(null));
            assertEquals("UUID cannot be null", exception.getMessage());
            assertEquals("INVALID_USER_ID", exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw exception for null UUID string")
        void shouldThrowExceptionForNullUuidString() {
            // When & Then
            InvalidUserIdException exception = assertThrows(
                    InvalidUserIdException.class,
                    () -> UserId.fromString(null));
            assertEquals("UUID string cannot be null or empty", exception.getMessage());
            assertEquals("INVALID_USER_ID", exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw exception for empty UUID string")
        void shouldThrowExceptionForEmptyUuidString() {
            // When & Then
            InvalidUserIdException exception = assertThrows(
                    InvalidUserIdException.class,
                    () -> UserId.fromString(""));
            assertEquals("UUID string cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for whitespace-only UUID string")
        void shouldThrowExceptionForWhitespaceOnlyUuidString() {
            // When & Then
            InvalidUserIdException exception = assertThrows(
                    InvalidUserIdException.class,
                    () -> UserId.fromString("   "));
            assertEquals("UUID string cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid UUID format")
        void shouldThrowExceptionForInvalidUuidFormat() {
            // Given
            String invalidUuid = "invalid-uuid-format";

            // When & Then
            InvalidUserIdException exception = assertThrows(
                    InvalidUserIdException.class,
                    () -> UserId.fromString(invalidUuid));
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
            UserId userId1 = UserId.of(uuid);
            UserId userId2 = UserId.of(uuid);

            // When & Then
            assertEquals(userId1, userId2);
            assertEquals(userId1.hashCode(), userId2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when UUIDs are different")
        void shouldNotBeEqualWhenUuidsAreDifferent() {
            // Given
            UserId userId1 = UserId.generate();
            UserId userId2 = UserId.generate();

            // When & Then
            assertNotEquals(userId1, userId2);
        }

        @Test
        @DisplayName("Should not be equal to null or different type")
        void shouldNotBeEqualToNullOrDifferentType() {
            // Given
            UserId userId = UserId.generate();

            // When & Then
            assertNotEquals(userId, null);
            assertNotEquals(userId, "some-string");
            assertNotEquals(userId, UUID.randomUUID());
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            UserId userId = UserId.generate();

            // When & Then
            assertEquals(userId, userId);
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
            UserId userId = UserId.of(uuid);

            // When
            String result = userId.toString();
            String asStringResult = userId.asString();

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
            UserId userId = UserId.of(uuid);

            // When
            UUID result = userId.getValue();

            // Then
            assertEquals(uuid, result);
            assertSame(uuid, result);
        }
    }
}