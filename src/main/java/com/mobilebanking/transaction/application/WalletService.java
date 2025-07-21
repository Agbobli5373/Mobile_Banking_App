package com.mobilebanking.transaction.application;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.shared.domain.exception.UserNotFoundException;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.infrastructure.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for wallet operations.
 * Handles balance retrieval and other wallet-related functionality.
 */
@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);
    private final UserRepository userRepository;

    public WalletService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the current balance for the authenticated user.
     *
     * @return the user's current balance
     * @throws UserNotFoundException if the user is not found
     * @throws AccessDeniedException if the user is not authenticated
     */
    @Transactional(readOnly = true)
    public Money getBalance() {
        UserId userId = getCurrentUserId();
        logger.info("Retrieving balance for user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", userId);
                    return new UserNotFoundException(userId);
                });

        logger.debug("Balance retrieved for user {}: {}", userId, user.getBalance());
        return user.getBalance();
    }

    /**
     * Retrieves the current balance for a specific user.
     * This method requires administrative privileges or ownership of the account.
     *
     * @param userId the ID of the user
     * @return the user's current balance
     * @throws UserNotFoundException if the user is not found
     * @throws AccessDeniedException if the requester is not authorized
     */
    @Transactional(readOnly = true)
    public Money getBalance(UserId userId) {
        // Ensure the authenticated user is accessing their own balance
        UserId currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            logger.warn("Unauthorized balance access attempt: {} tried to access {}", currentUserId, userId);
            throw new AccessDeniedException("Cannot access another user's balance");
        }

        logger.info("Retrieving balance for user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", userId);
                    return new UserNotFoundException(userId);
                });

        logger.debug("Balance retrieved for user {}: {}", userId, user.getBalance());
        return user.getBalance();
    }

    /**
     * Gets the current authenticated user's ID.
     *
     * @return the current user's ID
     * @throws AccessDeniedException if no user is authenticated
     */
    private UserId getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getName().equals("anonymousUser")) {
            logger.error("No authenticated user found");
            throw new AccessDeniedException("User not authenticated");
        }

        return UserId.fromString(authentication.getName());
    }
}