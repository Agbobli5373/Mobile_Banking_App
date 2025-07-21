package com.mobilebanking.user.application;

import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.shared.domain.exception.DuplicatePhoneNumberException;
import com.mobilebanking.shared.domain.exception.InvalidPhoneNumberException;
import com.mobilebanking.shared.domain.exception.InvalidPinException;
import com.mobilebanking.shared.domain.exception.InvalidUserNameException;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the UserRegistrationService.
 */
@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    private String validName;
    private String validPhoneNumber;
    private String validPin;

    @BeforeEach
    void setUp() {
        validName = "John Doe";
        validPhoneNumber = "+1234567890";
        validPin = "123456";
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        when(userRepository.existsByPhone(any(PhoneNumber.class))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User registeredUser = userRegistrationService.registerUser(validName, validPhoneNumber, validPin);

        // Then
        assertNotNull(registeredUser);
        assertEquals(validName, registeredUser.getName().getValue());
        assertEquals(validPhoneNumber, registeredUser.getPhone().getValue());
        assertEquals(0, registeredUser.getBalance().getAmount().doubleValue());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenPhoneNumberAlreadyExists() {
        // Given
        when(userRepository.existsByPhone(any(PhoneNumber.class))).thenReturn(true);

        // When & Then
        DuplicatePhoneNumberException exception = assertThrows(
                DuplicatePhoneNumberException.class,
                () -> userRegistrationService.registerUser(validName, validPhoneNumber, validPin));

        assertEquals(validPhoneNumber, exception.getPhoneNumber());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenNameIsInvalid() {
        // Given
        String invalidName = ""; // Empty name

        // When & Then
        assertThrows(
                InvalidUserNameException.class,
                () -> userRegistrationService.registerUser(invalidName, validPhoneNumber, validPin));

        verify(userRepository, never()).existsByPhone(any(PhoneNumber.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenPhoneNumberIsInvalid() {
        // Given
        String invalidPhoneNumber = "invalid"; // Invalid format

        // When & Then
        assertThrows(
                InvalidPhoneNumberException.class,
                () -> userRegistrationService.registerUser(validName, invalidPhoneNumber, validPin));

        verify(userRepository, never()).existsByPhone(any(PhoneNumber.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenPinIsInvalid() {
        // Given
        String invalidPin = "abc"; // Non-numeric

        // When & Then
        assertThrows(
                InvalidPinException.class,
                () -> userRegistrationService.registerUser(validName, validPhoneNumber, invalidPin));

        verify(userRepository, never()).save(any(User.class));
    }
}