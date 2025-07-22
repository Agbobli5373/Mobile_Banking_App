package com.mobilebanking.shared.api;

import com.mobilebanking.auth.domain.exception.InvalidCredentialsException;
import com.mobilebanking.shared.api.dto.ErrorResponse;
import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.shared.domain.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private ServletWebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/api/test");
        webRequest = new ServletWebRequest(servletRequest);
    }

    @Test
    void shouldHandleInsufficientFundsException() {
        // Given
        InsufficientFundsException exception = InsufficientFundsException.forTransfer(
                Money.of(10.0), Money.of(5.0));

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsufficientFunds(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().getError());
        System.out.println(response.getBody().getMessage());
        assertTrue(response.getBody().getMessage().contains("Cannot transfer 10.00: insufficient balance of 5.00"));
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleUserNotFoundException() {
        // Given
        UserNotFoundException exception = new UserNotFoundException(UserId.generate());

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserNotFound(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not Found", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("User not found"));
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleDuplicatePhoneNumberException() {

        // Given
        DuplicatePhoneNumberException exception = new DuplicatePhoneNumberException("already registered", "1234567890");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicatePhoneNumber(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("already registered"));
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleInvalidCredentialsException() {
        // Given
        InvalidCredentialsException exception = InvalidCredentialsException.invalidPhoneOrPin();

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCredentials(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Invalid phone number or PIN", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleSelfTransferException() {
        // Given
        SelfTransferException exception = new SelfTransferException();

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleSelfTransfer(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        System.out.println(response.getBody().getMessage());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Cannot transfer money to yourself", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleInvalidMoneyException() {
        // Given
        InvalidMoneyException exception = new InvalidMoneyException("Invalid amount");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidMoney(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid amount", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleAccessDeniedException() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden", response.getBody().getError());
        assertEquals("Access denied", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleBadCredentialsException() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Authentication failed", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleInvalidUserNameException() {
        // Given
        InvalidUserNameException exception = new InvalidUserNameException("Invalid user name");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidUserName(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid user name", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleInvalidPinException() {
        // Given
        InvalidPinException exception = new InvalidPinException("Invalid PIN");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidPin(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid PIN", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleInvalidTransactionIdException() {
        // Given
        InvalidTransactionIdException exception = new InvalidTransactionIdException("Invalid transaction ID");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidTransactionId(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid transaction ID", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleInvalidUserIdException() {
        // Given
        InvalidUserIdException exception = new InvalidUserIdException("Invalid user ID");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidUserId(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid user ID", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleGenericException() {
        // Given
        Exception exception = new RuntimeException("Something went wrong");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAllOtherExceptions(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }
}