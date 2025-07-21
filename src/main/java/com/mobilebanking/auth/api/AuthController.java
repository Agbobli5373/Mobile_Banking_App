package com.mobilebanking.auth.api;

import com.mobilebanking.auth.api.dto.LoginRequest;
import com.mobilebanking.auth.api.dto.LoginResponse;
import com.mobilebanking.auth.application.LoginService;
import com.mobilebanking.auth.domain.exception.InvalidCredentialsException;
import com.mobilebanking.shared.domain.exception.DuplicatePhoneNumberException;
import com.mobilebanking.shared.domain.exception.InvalidPhoneNumberException;
import com.mobilebanking.shared.domain.exception.InvalidPinException;
import com.mobilebanking.shared.domain.exception.InvalidUserNameException;
import com.mobilebanking.user.api.dto.UserRegistrationRequest;
import com.mobilebanking.user.api.dto.UserRegistrationResponse;
import com.mobilebanking.user.application.UserRegistrationService;
import com.mobilebanking.user.domain.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for authentication-related endpoints.
 * Handles user registration and login.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserRegistrationService userRegistrationService;
    private final LoginService loginService;

    public AuthController(UserRegistrationService userRegistrationService, LoginService loginService) {
        this.userRegistrationService = userRegistrationService;
        this.loginService = loginService;
    }

    /**
     * Endpoint for user registration.
     *
     * @param request the registration request
     * @return registration response
     */
    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        logger.info("Received registration request for phone number: {}", request.getPhoneNumber());

        try {
            User registeredUser = userRegistrationService.registerUser(
                    request.getName(),
                    request.getPhoneNumber(),
                    request.getPin());

            UserRegistrationResponse response = UserRegistrationResponse.success(
                    registeredUser.getId().asString(),
                    registeredUser.getName().getValue(),
                    registeredUser.getPhone().getValue());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DuplicatePhoneNumberException e) {
            logger.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(UserRegistrationResponse.failure("Phone number already registered"));
        } catch (InvalidUserNameException | InvalidPhoneNumberException | InvalidPinException e) {
            logger.warn("Registration failed due to validation error: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(UserRegistrationResponse.failure(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during registration", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UserRegistrationResponse.failure("An unexpected error occurred"));
        }
    }

    /**
     * Endpoint for user login.
     *
     * @param request the login request
     * @return login response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Received login request for phone number: {}", request.getPhoneNumber());

        try {
            String token = loginService.login(request.getPhoneNumber(), request.getPin());
            return ResponseEntity.ok(LoginResponse.success(token));
        } catch (InvalidCredentialsException e) {
            logger.warn("Login failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.failure("Invalid phone number or PIN"));
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.failure("An unexpected error occurred"));
        }
    }

    /**
     * Exception handler for validation errors.
     *
     * @param ex the validation exception
     * @return map of field errors
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}