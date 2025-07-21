package com.mobilebanking.user.application;

import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.shared.domain.exception.DuplicatePhoneNumberException;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.domain.UserName;
import com.mobilebanking.user.infrastructure.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for user registration.
 * Handles the registration workflow with validation and duplicate checking.
 */
@Service
public class UserRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationService.class);
    private final UserRepository userRepository;

    public UserRegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user with the provided details.
     * Validates input and checks for duplicate phone numbers.
     *
     * @param name        the user's name
     * @param phoneNumber the user's phone number
     * @param pin         the user's PIN
     * @return the registered user
     * @throws DuplicatePhoneNumberException if phone number already exists
     */
    @Transactional
    public User registerUser(String name, String phoneNumber, String pin) {
        logger.info("Registering new user with phone number: {}", phoneNumber);

        // Convert to domain objects (validation happens here)
        UserName userName = UserName.of(name);
        PhoneNumber phone = PhoneNumber.of(phoneNumber);

        // Check for duplicate phone number
        if (userRepository.existsByPhone(phone)) {
            logger.warn("Registration failed: Phone number already exists: {}", phoneNumber);
            throw DuplicatePhoneNumberException.forPhoneNumber(phone);
        }

        // Create and save user
        User user = User.create(userName, phone, pin);
        User savedUser = userRepository.save(user);

        logger.info("User registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }
}