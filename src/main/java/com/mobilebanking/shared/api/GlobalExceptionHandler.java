package com.mobilebanking.shared.api;

import com.mobilebanking.auth.domain.exception.InvalidCredentialsException;
import com.mobilebanking.shared.api.dto.ErrorResponse;
import com.mobilebanking.shared.domain.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Provides consistent error responses for different types of exceptions.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Common error messages
    private static final String ERROR_BAD_REQUEST = "Bad Request";
    private static final String ERROR_NOT_FOUND = "Not Found";
    private static final String ERROR_UNAUTHORIZED = "Unauthorized";
    private static final String ERROR_FORBIDDEN = "Forbidden";
    private static final String ERROR_CONFLICT = "Conflict";
    private static final String ERROR_INTERNAL = "Internal Server Error";

    // Common response messages
    private static final String MSG_ACCESS_DENIED = "Access denied";
    private static final String MSG_UNEXPECTED_ERROR = "An unexpected error occurred";
    private static final String MSG_AUTHENTICATION_FAILED = "Authentication failed";
    private static final String MSG_INVALID_CREDENTIALS = "Invalid phone number or PIN";
    private static final String MSG_USER_NOT_FOUND = "User not found";
    private static final String MSG_VALIDATION_ERROR = "Validation Error";
    private static final String MSG_CONSTRAINT_VALIDATION = "Constraint validation failed";

    /**
     * Handle validation exceptions from @Valid annotations.
     */
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ERROR_BAD_REQUEST,
                "Input validation failed",
                ((ServletWebRequest) request).getRequest().getRequestURI());

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorResponse.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle constraint violations from @Validated annotations.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ERROR_BAD_REQUEST,
                MSG_CONSTRAINT_VALIDATION,
                ((ServletWebRequest) request).getRequest().getRequestURI());

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(fieldName, message);
        });

        errorResponse.addValidationErrors(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle insufficient funds exception.
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(
            InsufficientFundsException ex,
            WebRequest request) {

        logger.warn("Insufficient funds: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ERROR_BAD_REQUEST,
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle user not found exception.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            WebRequest request) {

        logger.warn("User not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ERROR_NOT_FOUND,
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle duplicate phone number exception.
     */
    @ExceptionHandler(DuplicatePhoneNumberException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePhoneNumber(
            DuplicatePhoneNumberException ex,
            WebRequest request) {

        logger.warn("Duplicate phone number: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ERROR_CONFLICT,
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle invalid credentials exception.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            WebRequest request) {

        logger.warn("Invalid credentials: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ERROR_UNAUTHORIZED,
                MSG_INVALID_CREDENTIALS,
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle self transfer exception.
     */
    @ExceptionHandler(SelfTransferException.class)
    public ResponseEntity<ErrorResponse> handleSelfTransfer(
            SelfTransferException ex,
            WebRequest request) {

        logger.warn("Self transfer attempt: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ERROR_BAD_REQUEST,
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle invalid money exception.
     */
    @ExceptionHandler(InvalidMoneyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMoney(
            InvalidMoneyException ex,
            WebRequest request) {

        logger.warn("Invalid money value: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ERROR_BAD_REQUEST,
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle invalid phone number exception.
     */
    @ExceptionHandler(InvalidPhoneNumberException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPhoneNumber(
            InvalidPhoneNumberException ex,
            WebRequest request) {

        logger.warn("Invalid phone number: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ERROR_BAD_REQUEST,
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle invalid user name exception.
     */
    @ExceptionHandler(InvalidUserNameException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserName(
            InvalidUserNameException ex,
            WebRequest request) {

        logger.warn("Invalid user name: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ERROR_BAD_REQUEST,
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle invalid PIN exception.
     */
    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPin(
            InvalidPinException ex,
            WebRequest request) {

        logger.warn("Invalid PIN: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ERROR_BAD_REQUEST,
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle invalid transaction ID exception.
     */
    @ExceptionHandler(InvalidTransactionIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransactionId(
            InvalidTransactionIdException ex,
            WebRequest request) {

        logger.warn("Invalid transaction ID: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ERROR_BAD_REQUEST,
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle invalid user ID exception.
     */
    @ExceptionHandler(InvalidUserIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserId(
            InvalidUserIdException ex,
            WebRequest request) {

        logger.warn("Invalid user ID: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ERROR_BAD_REQUEST,
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle Spring Security authentication exceptions.
     */
    @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            Exception ex,
            WebRequest request) {

        logger.warn("Authentication error: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ERROR_UNAUTHORIZED,
                MSG_AUTHENTICATION_FAILED,
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle access denied exceptions.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        logger.warn("Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ERROR_FORBIDDEN,
                MSG_ACCESS_DENIED,
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllOtherExceptions(
            Exception ex,
            WebRequest request) {

        logger.error("Unhandled exception", ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ERROR_INTERNAL,
                MSG_UNEXPECTED_ERROR,
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}