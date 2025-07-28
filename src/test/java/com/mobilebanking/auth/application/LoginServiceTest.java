package com.mobilebanking.auth.application;

import com.mobilebanking.auth.domain.AuthenticationService;
import com.mobilebanking.auth.domain.JwtTokenService;
import com.mobilebanking.auth.domain.exception.InvalidCredentialsException;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.domain.UserName;
import com.mobilebanking.user.infrastructure.UserRepository;
import com.mobilebanking.observability.ObservabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private ObservabilityService observabilityService;

    private LoginService loginService;
    private User testUser;
    private String phoneNumber;
    private String pin;
    private String token;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(userRepository, authenticationService, jwtTokenService, observabilityService);

        phoneNumber = "+1234567890";
        pin = "123456";
        token = "test.jwt.token";

        UserId userId = UserId.of(UUID.randomUUID());
        testUser = User.create(UserName.of("Test User"), PhoneNumber.of(phoneNumber), pin);

        // Use reflection to access the private field for testing
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(testUser, userId.asString());
        } catch (Exception e) {
            fail("Failed to set user ID for testing");
        }
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        PhoneNumber phone = PhoneNumber.of(phoneNumber);
        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(testUser));
        when(authenticationService.validateCredentials(testUser, pin)).thenReturn(true);
        when(jwtTokenService.generateToken(testUser.getId())).thenReturn(token);

        // When
        String resultToken = loginService.login(phoneNumber, pin);

        // Then
        assertEquals(token, resultToken);
        verify(userRepository).findByPhone(phone);
        verify(authenticationService).validateCredentials(testUser, pin);
        verify(jwtTokenService).generateToken(testUser.getId());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        PhoneNumber phone = PhoneNumber.of(phoneNumber);
        when(userRepository.findByPhone(phone)).thenReturn(Optional.empty());

        // When & Then
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(phoneNumber, pin));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByPhone(phone);
        verifyNoInteractions(authenticationService);
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void shouldThrowExceptionWhenCredentialsInvalid() {
        // Given
        PhoneNumber phone = PhoneNumber.of(phoneNumber);
        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(testUser));
        when(authenticationService.validateCredentials(testUser, pin)).thenReturn(false);

        // When & Then
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(phoneNumber, pin));
        assertEquals("Invalid phone number or PIN", exception.getMessage());
        verify(userRepository).findByPhone(phone);
        verify(authenticationService).validateCredentials(testUser, pin);
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void shouldThrowExceptionWhenPhoneNumberIsNull() {
        // When & Then
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(null, pin));
        assertEquals("Invalid phone number or PIN", exception.getMessage());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(authenticationService);
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void shouldThrowExceptionWhenPinIsNull() {
        // When & Then
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(phoneNumber, null));
        assertEquals("Invalid phone number or PIN", exception.getMessage());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(authenticationService);
        verifyNoInteractions(jwtTokenService);
    }
}