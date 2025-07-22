package com.mobilebanking.transaction.application;

import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.shared.domain.exception.UserNotFoundException;
import com.mobilebanking.transaction.domain.Transaction;
import com.mobilebanking.transaction.infrastructure.TransactionRepository;
import com.mobilebanking.user.infrastructure.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service for transaction query operations.
 * Handles transaction history retrieval with proper filtering and
 * authorization.
 */
@Service
public class TransactionQueryService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionQueryService.class);
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionQueryService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves transaction history for the authenticated user.
     * Returns all transactions where the user is either sender or receiver,
     * ordered by timestamp in descending order (most recent first).
     *
     * @return List of transactions involving the authenticated user
     * @throws UserNotFoundException if the authenticated user is not found
     * @throws AccessDeniedException if the user is not authenticated
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionHistory() {
        UserId userId = getCurrentUserId();
        logger.info("Retrieving transaction history for user: {}", userId);

        // Verify user exists
        if (!userRepository.existsByUserId(userId)) {
            logger.error("User not found: {}", userId);
            throw new UserNotFoundException(userId);
        }

        // Get all transactions involving the user, ordered by timestamp descending
        List<Transaction> transactions = transactionRepository.findByUserOrderByTimestampDesc(userId);

        logger.info("Retrieved {} transactions for user: {}", transactions.size(), userId);
        return transactions;
    }

    /**
     * Retrieves transaction history for a specific user.
     * This method ensures the authenticated user can only access their own
     * transaction history.
     *
     * @param userId the ID of the user whose transaction history to retrieve
     * @return List of transactions involving the specified user
     * @throws UserNotFoundException if the user is not found
     * @throws AccessDeniedException if the requester is not authorized to access
     *                               the transaction history
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionHistory(UserId userId) {
        // Ensure the authenticated user is accessing their own transaction history
        UserId currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            logger.warn("Unauthorized transaction history access attempt: {} tried to access {}", currentUserId,
                    userId);
            throw new AccessDeniedException("Cannot access another user's transaction history");
        }

        logger.info("Retrieving transaction history for user: {}", userId);

        // Verify user exists
        if (!userRepository.existsByUserId(userId)) {
            logger.error("User not found: {}", userId);
            throw new UserNotFoundException(userId);
        }

        // Get all transactions involving the user, ordered by timestamp descending
        List<Transaction> transactions = transactionRepository.findByUserOrderByTimestampDesc(userId);

        logger.info("Retrieved {} transactions for user: {}", transactions.size(), userId);
        return transactions;
    }

    /**
     * Gets the current authenticated user's ID.
     *
     * @return the current user's ID
     * @throws AccessDeniedException if no user is authenticated
     */
    protected UserId getCurrentUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getName().equals("anonymousUser")) {
            logger.error("No authenticated user found");
            throw new AccessDeniedException("User not authenticated");
        }

        return UserId.fromString(authentication.getName());
    }

    /**
     * Gets the current authentication from SecurityContext.
     * This method is protected to allow for easier testing.
     *
     * @return the current authentication
     */
    protected Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Retrieves paginated transaction history for the authenticated user.
     * Returns a page of transactions where the user is either sender or receiver,
     * ordered by timestamp in descending order (most recent first).
     *
     * @param pageable pagination information
     * @return Page of transactions involving the authenticated user
     * @throws UserNotFoundException if the authenticated user is not found
     * @throws AccessDeniedException if the user is not authenticated
     */
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionHistoryPaginated(Pageable pageable) {
        UserId userId = getCurrentUserId();
        logger.info("Retrieving paginated transaction history for user: {}, page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        // Verify user exists
        if (!userRepository.existsByUserId(userId)) {
            logger.error("User not found: {}", userId);
            throw new UserNotFoundException(userId);
        }

        // Get paginated transactions involving the user, ordered by timestamp
        // descending
        Page<Transaction> transactionsPage = transactionRepository.findByUserOrderByTimestampDesc(userId, pageable);

        logger.info("Retrieved page {} of {} with {} transactions for user: {}",
                transactionsPage.getNumber(), transactionsPage.getTotalPages(),
                transactionsPage.getNumberOfElements(), userId);
        return transactionsPage;
    }

    /**
     * Retrieves paginated transaction history for a specific user.
     * This method ensures the authenticated user can only access their own
     * transaction history.
     *
     * @param userId   the ID of the user whose transaction history to retrieve
     * @param pageable pagination information
     * @return Page of transactions involving the specified user
     * @throws UserNotFoundException if the user is not found
     * @throws AccessDeniedException if the requester is not authorized to access
     *                               the transaction history
     */
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionHistoryPaginated(UserId userId, Pageable pageable) {
        // Ensure the authenticated user is accessing their own transaction history
        UserId currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            logger.warn("Unauthorized transaction history access attempt: {} tried to access {}", currentUserId,
                    userId);
            throw new AccessDeniedException("Cannot access another user's transaction history");
        }

        logger.info("Retrieving paginated transaction history for user: {}, page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        // Verify user exists
        if (!userRepository.existsByUserId(userId)) {
            logger.error("User not found: {}", userId);
            throw new UserNotFoundException(userId);
        }

        // Get paginated transactions involving the user, ordered by timestamp
        // descending
        Page<Transaction> transactionsPage = transactionRepository.findByUserOrderByTimestampDesc(userId, pageable);

        logger.info("Retrieved page {} of {} with {} transactions for user: {}",
                transactionsPage.getNumber(), transactionsPage.getTotalPages(),
                transactionsPage.getNumberOfElements(), userId);
        return transactionsPage;
    }
}