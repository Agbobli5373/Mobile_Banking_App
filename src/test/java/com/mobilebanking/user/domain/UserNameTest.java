package com.mobilebanking.user.domain;

import com.mobilebanking.shared.domain.exception.InvalidUserNameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserName Value Object Tests")
class UserNameTest {

    @Test
    @DisplayName("Should create UserName from valid name")
    void shouldCreateUserNameFromValidName() {
        // Given
        String name = "John Doe";

        // When
        UserName userName = UserName.of(name);

        // Then
        assertThat(userName).isNotNull();
        assertThat(userName.getValue()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should reject names with leading or trailing spaces")
    void shouldRejectNamesWithLeadingOrTrailingSpaces() {
        // When & Then
        assertThatThrownBy(() -> UserName.of("  John Doe  "))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("User name cannot start or end with spaces, hyphens, or apostrophes");
    }

    @ParameterizedTest
    @ValueSource(strings = { "John", "Mary Jane", "Jean-Pierre", "O'Connor", "Anna-Maria Smith" })
    @DisplayName("Should accept valid names")
    void shouldAcceptValidNames(String validName) {
        // When & Then
        assertThatNoException().isThrownBy(() -> UserName.of(validName));
    }

    @Test
    @DisplayName("Should format display name with proper capitalization")
    void shouldFormatDisplayNameWithProperCapitalization() {
        // Given
        UserName userName1 = UserName.of("john doe");
        UserName userName2 = UserName.of("MARY JANE");
        UserName userName3 = UserName.of("jean-pierre");

        // When & Then
        assertThat(userName1.getDisplayValue()).isEqualTo("John Doe");
        assertThat(userName2.getDisplayValue()).isEqualTo("Mary Jane");
        assertThat(userName3.getDisplayValue()).isEqualTo("Jean-pierre");
    }

    @Test
    @DisplayName("Should reject null name")
    void shouldRejectNullName() {
        // When & Then
        assertThatThrownBy(() -> UserName.of(null))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("User name cannot be null or empty");
    }

    @Test
    @DisplayName("Should reject empty name")
    void shouldRejectEmptyName() {
        // When & Then
        assertThatThrownBy(() -> UserName.of(""))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("User name cannot be null or empty");
    }

    @Test
    @DisplayName("Should reject whitespace-only name")
    void shouldRejectWhitespaceOnlyName() {
        // When & Then
        assertThatThrownBy(() -> UserName.of("   "))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("User name cannot be null or empty");
    }

    @Test
    @DisplayName("Should reject name that is too short")
    void shouldRejectNameThatIsTooShort() {
        // When & Then
        assertThatThrownBy(() -> UserName.of("A"))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("User name must be between 2 and 50 characters");
    }

    @Test
    @DisplayName("Should reject name that is too long")
    void shouldRejectNameThatIsTooLong() {
        // Given
        String longName = "A".repeat(51);

        // When & Then
        assertThatThrownBy(() -> UserName.of(longName))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("User name must be between 2 and 50 characters");
    }

    @ParameterizedTest
    @ValueSource(strings = { "John123", "Mary@Doe", "John.Doe", "Mary#Jane", "John$Doe" })
    @DisplayName("Should reject names with invalid characters")
    void shouldRejectNamesWithInvalidCharacters(String invalidName) {
        // When & Then
        assertThatThrownBy(() -> UserName.of(invalidName))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("User name can only contain letters, spaces, hyphens, and apostrophes");
    }

    @ParameterizedTest
    @ValueSource(strings = { " John", "John ", "-John", "John-", "'John", "John'" })
    @DisplayName("Should reject names starting or ending with special characters")
    void shouldRejectNamesStartingOrEndingWithSpecialCharacters(String invalidName) {
        // When & Then
        assertThatThrownBy(() -> UserName.of(invalidName))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("User name cannot start or end with spaces, hyphens, or apostrophes");
    }

    @ParameterizedTest
    @ValueSource(strings = { "John  Doe", "Mary--Jane", "John''Doe", "Mary- Jane", "John ' Doe" })
    @DisplayName("Should reject names with consecutive special characters")
    void shouldRejectNamesWithConsecutiveSpecialCharacters(String invalidName) {
        // When & Then
        assertThatThrownBy(() -> UserName.of(invalidName))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("User name cannot contain consecutive special characters");
    }

    @Test
    @DisplayName("Should be equal when values are same")
    void shouldBeEqualWhenValuesAreSame() {
        // Given
        UserName name1 = UserName.of("John Doe");
        UserName name2 = UserName.of("John Doe");

        // When & Then
        assertThat(name1).isEqualTo(name2);
        assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when values are different")
    void shouldNotBeEqualWhenValuesAreDifferent() {
        // Given
        UserName name1 = UserName.of("John Doe");
        UserName name2 = UserName.of("Jane Smith");

        // When & Then
        assertThat(name1).isNotEqualTo(name2);
    }

    @Test
    @DisplayName("Should return value in toString")
    void shouldReturnValueInToString() {
        // Given
        UserName userName = UserName.of("John Doe");

        // When & Then
        assertThat(userName.toString()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should handle single word names")
    void shouldHandleSingleWordNames() {
        // Given
        UserName userName = UserName.of("Madonna");

        // When & Then
        assertThat(userName.getValue()).isEqualTo("Madonna");
        assertThat(userName.getDisplayValue()).isEqualTo("Madonna");
    }

    @Test
    @DisplayName("Should handle names with multiple spaces")
    void shouldHandleNamesWithMultipleSpaces() {
        // Given - This should be rejected due to consecutive spaces
        // When & Then
        assertThatThrownBy(() -> UserName.of("John   Doe"))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("User name cannot contain consecutive special characters");
    }
}