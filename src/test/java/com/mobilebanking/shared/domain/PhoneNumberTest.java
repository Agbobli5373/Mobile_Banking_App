package com.mobilebanking.shared.domain;

import com.mobilebanking.shared.domain.exception.InvalidPhoneNumberException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PhoneNumber Value Object Tests")
class PhoneNumberTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create PhoneNumber from valid phone number")
        void shouldCreatePhoneNumberFromValidPhoneNumber() {
            // Given
            String phoneNumber = "+1234567890";

            // When
            PhoneNumber phone = PhoneNumber.of(phoneNumber);

            // Then
            assertEquals("+1234567890", phone.getValue());
        }

        @Test
        @DisplayName("Should create PhoneNumber from phone number without plus")
        void shouldCreatePhoneNumberFromPhoneNumberWithoutPlus() {
            // Given
            String phoneNumber = "1234567890";

            // When
            PhoneNumber phone = PhoneNumber.of(phoneNumber);

            // Then
            assertEquals("1234567890", phone.getValue());
        }

        @Test
        @DisplayName("Should normalize phone number by removing spaces and dashes")
        void shouldNormalizePhoneNumberByRemovingSpacesAndDashes() {
            // Given
            String phoneNumber = "+1 (234) 567-890";

            // When
            PhoneNumber phone = PhoneNumber.of(phoneNumber);

            // Then
            assertEquals("+1234567890", phone.getValue());
        }

        @Test
        @DisplayName("Should normalize phone number by removing parentheses")
        void shouldNormalizePhoneNumberByRemovingParentheses() {
            // Given
            String phoneNumber = "(234) 567-8901";

            // When
            PhoneNumber phone = PhoneNumber.of(phoneNumber);

            // Then
            assertEquals("2345678901", phone.getValue());
        }

        @Test
        @DisplayName("Should create PhoneNumber from minimum length number")
        void shouldCreatePhoneNumberFromMinimumLengthNumber() {
            // Given
            String phoneNumber = "1234567"; // 7 digits minimum

            // When
            PhoneNumber phone = PhoneNumber.of(phoneNumber);

            // Then
            assertEquals("1234567", phone.getValue());
        }

        @Test
        @DisplayName("Should create PhoneNumber from maximum length number")
        void shouldCreatePhoneNumberFromMaximumLengthNumber() {
            // Given
            String phoneNumber = "+123456789012345"; // 15 digits maximum

            // When
            PhoneNumber phone = PhoneNumber.of(phoneNumber);

            // Then
            assertEquals("+123456789012345", phone.getValue());
        }

        @Test
        @DisplayName("Should throw exception for null phone number")
        void shouldThrowExceptionForNullPhoneNumber() {
            // When & Then
            InvalidPhoneNumberException exception = assertThrows(
                    InvalidPhoneNumberException.class,
                    () -> PhoneNumber.of(null));
            assertEquals("Phone number cannot be null or empty", exception.getMessage());
            assertEquals("INVALID_PHONE_NUMBER", exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw exception for empty phone number")
        void shouldThrowExceptionForEmptyPhoneNumber() {
            // When & Then
            InvalidPhoneNumberException exception = assertThrows(
                    InvalidPhoneNumberException.class,
                    () -> PhoneNumber.of(""));
            assertEquals("Phone number cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for whitespace-only phone number")
        void shouldThrowExceptionForWhitespaceOnlyPhoneNumber() {
            // When & Then
            InvalidPhoneNumberException exception = assertThrows(
                    InvalidPhoneNumberException.class,
                    () -> PhoneNumber.of("   "));
            assertEquals("Phone number cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for phone number too short")
        void shouldThrowExceptionForPhoneNumberTooShort() {
            // Given
            String shortPhoneNumber = "123456"; // 6 digits, minimum is 7

            // When & Then
            InvalidPhoneNumberException exception = assertThrows(
                    InvalidPhoneNumberException.class,
                    () -> PhoneNumber.of(shortPhoneNumber));
            assertEquals("Phone number must be between 7 and 15 digits", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for phone number too long")
        void shouldThrowExceptionForPhoneNumberTooLong() {
            // Given
            String longPhoneNumber = "+1234567890123456"; // 16 digits, maximum is 15

            // When & Then
            InvalidPhoneNumberException exception = assertThrows(
                    InvalidPhoneNumberException.class,
                    () -> PhoneNumber.of(longPhoneNumber));
            assertEquals("Phone number must be between 7 and 15 digits", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for phone number with invalid characters")
        void shouldThrowExceptionForPhoneNumberWithInvalidCharacters() {
            // Given
            String invalidPhoneNumber = "123abc7890";

            // When & Then
            InvalidPhoneNumberException exception = assertThrows(
                    InvalidPhoneNumberException.class,
                    () -> PhoneNumber.of(invalidPhoneNumber));
            assertEquals("Invalid phone number format", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for phone number starting with zero")
        void shouldThrowExceptionForPhoneNumberStartingWithZero() {
            // Given
            String invalidPhoneNumber = "0123456789";

            // When & Then
            InvalidPhoneNumberException exception = assertThrows(
                    InvalidPhoneNumberException.class,
                    () -> PhoneNumber.of(invalidPhoneNumber));
            assertEquals("Invalid phone number format", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for phone number starting with plus zero")
        void shouldThrowExceptionForPhoneNumberStartingWithPlusZero() {
            // Given
            String invalidPhoneNumber = "+0123456789";

            // When & Then
            InvalidPhoneNumberException exception = assertThrows(
                    InvalidPhoneNumberException.class,
                    () -> PhoneNumber.of(invalidPhoneNumber));
            assertEquals("Invalid phone number format", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Display Value Tests")
    class DisplayValueTests {

        @Test
        @DisplayName("Should return display value with plus for number without plus")
        void shouldReturnDisplayValueWithPlusForNumberWithoutPlus() {
            // Given
            PhoneNumber phone = PhoneNumber.of("1234567890");

            // When
            String displayValue = phone.getDisplayValue();

            // Then
            assertEquals("+1234567890", displayValue);
        }

        @Test
        @DisplayName("Should return display value as-is for number with plus")
        void shouldReturnDisplayValueAsIsForNumberWithPlus() {
            // Given
            PhoneNumber phone = PhoneNumber.of("+1234567890");

            // When
            String displayValue = phone.getDisplayValue();

            // Then
            assertEquals("+1234567890", displayValue);
        }
    }

    @Nested
    @DisplayName("Equality and Hash Tests")
    class EqualityAndHashTests {

        @Test
        @DisplayName("Should be equal when phone numbers are the same")
        void shouldBeEqualWhenPhoneNumbersAreTheSame() {
            // Given
            PhoneNumber phone1 = PhoneNumber.of("+1234567890");
            PhoneNumber phone2 = PhoneNumber.of("+1234567890");

            // When & Then
            assertEquals(phone1, phone2);
            assertEquals(phone1.hashCode(), phone2.hashCode());
        }

        @Test
        @DisplayName("Should be equal when normalized phone numbers are the same")
        void shouldBeEqualWhenNormalizedPhoneNumbersAreTheSame() {
            // Given
            PhoneNumber phone1 = PhoneNumber.of("+1 (234) 567-890");
            PhoneNumber phone2 = PhoneNumber.of("+1234567890");

            // When & Then
            assertEquals(phone1, phone2);
            assertEquals(phone1.hashCode(), phone2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when phone numbers are different")
        void shouldNotBeEqualWhenPhoneNumbersAreDifferent() {
            // Given
            PhoneNumber phone1 = PhoneNumber.of("+1234567890");
            PhoneNumber phone2 = PhoneNumber.of("+9876543210");

            // When & Then
            assertNotEquals(phone1, phone2);
        }

        @Test
        @DisplayName("Should not be equal to null or different type")
        void shouldNotBeEqualToNullOrDifferentType() {
            // Given
            PhoneNumber phone = PhoneNumber.of("+1234567890");

            // When & Then
            assertNotEquals(phone, null);
            assertNotEquals(phone, "+1234567890");
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            PhoneNumber phone = PhoneNumber.of("+1234567890");

            // When & Then
            assertEquals(phone, phone);
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should return correct string representation")
        void shouldReturnCorrectStringRepresentation() {
            // Given
            PhoneNumber phone = PhoneNumber.of("+1234567890");

            // When
            String result = phone.toString();

            // Then
            assertEquals("+1234567890", result);
        }

        @Test
        @DisplayName("Should return normalized value in string representation")
        void shouldReturnNormalizedValueInStringRepresentation() {
            // Given
            PhoneNumber phone = PhoneNumber.of("+1 (234) 567-890");

            // When
            String result = phone.toString();

            // Then
            assertEquals("+1234567890", result);
        }
    }

    @Nested
    @DisplayName("Value Access Tests")
    class ValueAccessTests {

        @Test
        @DisplayName("Should return correct phone number value")
        void shouldReturnCorrectPhoneNumberValue() {
            // Given
            PhoneNumber phone = PhoneNumber.of("+1234567890");

            // When
            String result = phone.getValue();

            // Then
            assertEquals("+1234567890", result);
        }

        @Test
        @DisplayName("Should return normalized value")
        void shouldReturnNormalizedValue() {
            // Given
            PhoneNumber phone = PhoneNumber.of("+1 (234) 567-890");

            // When
            String result = phone.getValue();

            // Then
            assertEquals("+1234567890", result);
        }
    }
}