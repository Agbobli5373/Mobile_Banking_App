package com.mobilebanking.auth.domain;

import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.user.domain.HashedPin;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.domain.UserName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {

    private AuthenticationService authenticationService;
    private User user;
    private final String rawPin = "1234";

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService();

        // Create a test user with a known PIN
        UserName userName = UserName.of("Test User");
        PhoneNumber phoneNumber = PhoneNumber.of("1234567890");
        user = User.create(userName, phoneNumber, rawPin);
    }

    @Test
    void validateCredentials_withValidPin_shouldReturnTrue() {
        // When
        boolean result = authenticationService.validateCredentials(user, rawPin);

        // Then
        assertTrue(result);
    }

    @Test
    void validateCredentials_withInvalidPin_shouldReturnFalse() {
        // When
        boolean result = authenticationService.validateCredentials(user, "9999");

        // Then
        assertFalse(result);
    }

    @Test
    void validateCredentials_withNullUser_shouldThrowException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> authenticationService.validateCredentials(null, rawPin));
    }

    @Test
    void validateCredentials_withNullPin_shouldThrowException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> authenticationService.validateCredentials(user, null));
    }
}