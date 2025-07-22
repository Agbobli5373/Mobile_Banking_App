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

        User registeredUser = userRegistrationService.registerUser(
                request.getName(),
                request.getPhoneNumber(),
                request.getPin());

        UserRegistrationResponse response = UserRegistrationResponse.success(
                registeredUser.getId().asString(),
                registeredUser.getName().getValue(),
                registeredUser.getPhone().getValue());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

        String token = loginService.login(request.getPhoneNumber(), request.getPin());
        return ResponseEntity.ok(LoginResponse.success(token));
    }
}