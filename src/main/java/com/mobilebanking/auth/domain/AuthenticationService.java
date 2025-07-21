package com.mobilebanking.auth.domain;

import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.user.domain.User;

/**
 * Domain service for handling authentication logic.
 * Responsible for validating user credentials.
 */
public class AuthenticationService {

    /**
     * Validates user credentials (phone number and PIN).
     *
     * @param user   the user to authenticate
     * @param rawPin the raw PIN to validate
     * @return true if credentials are valid, false otherwise
     * @throws IllegalArgumentException if user or rawPin is null
     */
    public boolean validateCredentials(User user, String rawPin) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (rawPin == null) {
            throw new IllegalArgumentException("PIN cannot be null");
        }

        return user.hasValidPin(rawPin);
    }
}