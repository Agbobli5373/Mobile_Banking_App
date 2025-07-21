package com.mobilebanking.auth.api;

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

    public AuthController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
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