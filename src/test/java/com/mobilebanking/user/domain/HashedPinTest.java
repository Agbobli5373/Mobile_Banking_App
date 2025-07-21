package com.mobilebanking.user.domain;

import com.mobilebanking.shared.domain.exception.InvalidPinException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("HashedPin Value Object Tests")
class HashedPinTest {

    @Test
    @DisplayName("Should create HashedPin from valid raw PIN")
    void shouldCreateHashedPinFromValidRawPin() {
        // Given
        String rawPin = "1234";

        // When
        HashedPin hashedPin = HashedPin.fromRawPin(rawPin);

        // Then
        assertThat(hashedPin).isNotNull();
        assertThat(hashedPin.getHashedValue()).isNotNull();
        assertThat(hashedPin.getHashedValue()).isNotEqualTo(rawPin);
        assertThat(hashedPin.getHashedValue()).startsWith("$2a$"); // BCrypt format
    }

    @Test
    @DisplayName("Should validate correct PIN")
    void shouldValidateCorrectPin() {
        // Given
        String rawPin = "1234";
        HashedPin hashedPin = HashedPin.fromRawPin(rawPin);

        // When & Then
        assertThat(hashedPin.matches(rawPin)).isTrue();
    }

    @Test
    @DisplayName("Should reject incorrect PIN")
    void shouldRejectIncorrectPin() {
        // Given
        String rawPin = "1234";
        String wrongPin = "5678";
        HashedPin hashedPin = HashedPin.fromRawPin(rawPin);

        // When & Then
        assertThat(hashedPin.matches(wrongPin)).isFalse();
    }

    @Test
    @DisplayName("Should create HashedPin from existing hash")
    void shouldCreateHashedPinFromExistingHash() {
        // Given
        String rawPin = "1234";
        HashedPin originalPin = HashedPin.fromRawPin(rawPin);
        String existingHash = originalPin.getHashedValue();

        // When
        HashedPin reconstructedPin = HashedPin.fromHashedValue(existingHash);

        // Then
        assertThat(reconstructedPin).isNotNull();
        assertThat(reconstructedPin.getHashedValue()).isEqualTo(existingHash);
        assertThat(reconstructedPin.matches(rawPin)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "123", "12345678" })
    @DisplayName("Should reject invalid PIN lengths")
    void shouldRejectInvalidPinLengths(String invalidPin) {
        // When & Then
        assertThatThrownBy(() -> HashedPin.fromRawPin(invalidPin))
                .isInstanceOf(InvalidPinException.class)
                .hasMessageContaining("PIN must be between 4 and 6 digits");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   " })
    @DisplayName("Should reject empty or whitespace-only PINs")
    void shouldRejectEmptyOrWhitespaceOnlyPins(String invalidPin) {
        // When & Then
        assertThatThrownBy(() -> HashedPin.fromRawPin(invalidPin))
                .isInstanceOf(InvalidPinException.class)
                .hasMessageContaining("PIN cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = { "12a4", "12-4", "12 4", "abcd", "12.4" })
    @DisplayName("Should reject non-numeric PINs")
    void shouldRejectNonNumericPins(String invalidPin) {
        // When & Then
        assertThatThrownBy(() -> HashedPin.fromRawPin(invalidPin))
                .isInstanceOf(InvalidPinException.class)
                .hasMessageContaining("PIN must contain only numeric digits");
    }

    @Test
    @DisplayName("Should reject null raw PIN")
    void shouldRejectNullRawPin() {
        // When & Then
        assertThatThrownBy(() -> HashedPin.fromRawPin(null))
                .isInstanceOf(InvalidPinException.class)
                .hasMessageContaining("PIN cannot be null or empty");
    }

    @Test
    @DisplayName("Should reject null hashed value")
    void shouldRejectNullHashedValue() {
        // When & Then
        assertThatThrownBy(() -> HashedPin.fromHashedValue(null))
                .isInstanceOf(InvalidPinException.class)
                .hasMessageContaining("Hashed PIN cannot be null or empty");
    }

    @Test
    @DisplayName("Should reject empty hashed value")
    void shouldRejectEmptyHashedValue() {
        // When & Then
        assertThatThrownBy(() -> HashedPin.fromHashedValue(""))
                .isInstanceOf(InvalidPinException.class)
                .hasMessageContaining("Hashed PIN cannot be null or empty");
    }

    @Test
    @DisplayName("Should reject null PIN in matches method")
    void shouldRejectNullPinInMatches() {
        // Given
        HashedPin hashedPin = HashedPin.fromRawPin("1234");

        // When & Then
        assertThatThrownBy(() -> hashedPin.matches(null))
                .isInstanceOf(InvalidPinException.class)
                .hasMessageContaining("PIN cannot be null or empty");
    }

    @Test
    @DisplayName("Should handle PIN with leading/trailing spaces")
    void shouldHandlePinWithSpaces() {
        // Given
        String rawPin = "  1234  ";

        // When
        HashedPin hashedPin = HashedPin.fromRawPin(rawPin);

        // Then
        assertThat(hashedPin.matches("1234")).isTrue();
        assertThat(hashedPin.matches(rawPin)).isTrue();
    }

    @Test
    @DisplayName("Should be equal when hashed values are same")
    void shouldBeEqualWhenHashedValuesAreSame() {
        // Given
        String rawPin = "1234";
        HashedPin pin1 = HashedPin.fromRawPin(rawPin);
        HashedPin pin2 = HashedPin.fromHashedValue(pin1.getHashedValue());

        // When & Then
        assertThat(pin1).isEqualTo(pin2);
        assertThat(pin1.hashCode()).isEqualTo(pin2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when hashed values are different")
    void shouldNotBeEqualWhenHashedValuesAreDifferent() {
        // Given
        HashedPin pin1 = HashedPin.fromRawPin("1234");
        HashedPin pin2 = HashedPin.fromRawPin("5678");

        // When & Then
        assertThat(pin1).isNotEqualTo(pin2);
    }

    @Test
    @DisplayName("Should not expose hash in toString")
    void shouldNotExposeHashInToString() {
        // Given
        HashedPin hashedPin = HashedPin.fromRawPin("1234");

        // When
        String toString = hashedPin.toString();

        // Then
        assertThat(toString).isEqualTo("[PROTECTED]");
        assertThat(toString).doesNotContain(hashedPin.getHashedValue());
    }
}