package com.mobilebanking.auth.application;

import com.mobilebanking.auth.domain.AuthenticationService;
import com.mobilebanking.auth.domain.JwtTokenService;
import com.mobilebanking.auth.domain.exception.InvalidCredentialsException;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for handling user login operations.
 * Orchestrates the authentication workflow and token generation.
 */
@Service
public class LoginService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final JwtTokenService jwtTokenService;

    public LoginService(
            UserRepository userRepository,
            AuthenticationService authenticationService,
            JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Authenticates a user with phone number and PIN, generating a JWT token upon
     * success.
     *
     * @param phoneNumber the user's phone number
     * @param pin         the user's PIN
     * @return JWT token for authenticated user
     * @throws InvalidCredentialsException if credentials are invalid or user not
     *                                     found
     */
    @Transactional(readOnly = true)
    public String login(String phoneNumber, String pin) {
        // Validate input
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw InvalidCredentialsException.invalidPhoneOrPin();
        }
        if (pin == null || pin.trim().isEmpty()) {
            throw InvalidCredentialsException.invalidPhoneOrPin();
        }

        try {
            // Find user by phone number
            PhoneNumber phone = PhoneNumber.of(phoneNumber);
            User user = userRepository.findByPhone(phone)
                    .orElseThrow(InvalidCredentialsException::userNotFound);

            // Validate credentials
            if (!authenticationService.validateCredentials(user, pin)) {
                throw InvalidCredentialsException.invalidPhoneOrPin();
            }

            // Generate JWT token
            return jwtTokenService.generateToken(user.getId());
        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw InvalidCredentialsException.invalidPhoneOrPin();
        }
    }
}