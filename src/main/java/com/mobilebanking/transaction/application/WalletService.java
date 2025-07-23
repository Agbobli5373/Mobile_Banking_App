package com.mobilebanking.transaction.application;

import com.mobilebanking.notification.domain.NotificationService;
import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.shared.domain.exception.InsufficientFundsException;
import com.mobilebanking.shared.domain.exception.UserNotFoundException;
import com.mobilebanking.transaction.domain.MoneyTransferService;
import com.mobilebanking.transaction.domain.Transaction;
import com.mobilebanking.transaction.infrastructure.TransactionRepository;
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
    private final TransactionRepository transactionRepository;
    private final MoneyTransferService moneyTransferService;
    private final NotificationService notificationService;

    public WalletService(UserRepository userRepository,
            TransactionRepository transactionRepository,
            MoneyTransferService moneyTransferService,
            NotificationService notificationService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.moneyTransferService = moneyTransferService;
        this.notificationService = notificationService;
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
     * Transfers money from the authenticated user to another user identified by
     * phone number.
     * This operation is atomic - either both accounts are updated or none are.
     *
     * @param recipientPhone the phone number of the recipient
     * @param amount         the amount to transfer
     * @return the transaction ID of the completed transfer
     * @throws UserNotFoundException      if the recipient is not found
     * @throws InsufficientFundsException if the sender has insufficient funds
     * @throws IllegalArgumentException   if the transfer request is invalid
     * @throws AccessDeniedException      if the user is not authenticated
     */
    @Transactional
    public Transaction transferMoney(String recipientPhone, Money amount) {
        logger.info("Processing money transfer request to phone: {}, amount: {}", recipientPhone, amount);

        // Get the authenticated user (sender) with pessimistic lock
        UserId senderId = getCurrentUserId();
        User sender = userRepository.findByUserIdForUpdate(senderId.asString())
                .orElseThrow(() -> {
                    logger.error("Sender not found: {}", senderId);
                    return new UserNotFoundException(senderId);
                });

        // Find the recipient by phone number with pessimistic lock
        PhoneNumber recipientPhoneObj = PhoneNumber.of(recipientPhone);
        User recipient = userRepository.findByPhoneForUpdate(recipientPhoneObj)
                .orElseThrow(() -> {
                    logger.error("Recipient not found with phone: {}", recipientPhone);
                    return new UserNotFoundException("User with phone " + recipientPhone + " not found");
                });

        // Validate the transfer (will throw exceptions if invalid)
        moneyTransferService.validateTransferRequest(
                sender.getId(),
                recipient.getId(),
                amount,
                sender.getBalance());

        // Create the transaction record
        Transaction transaction = Transaction.createTransfer(sender.getId(), recipient.getId(), amount);

        // Update balances atomically
        sender.debitBalance(amount);
        recipient.creditBalance(amount);

        // Save all changes
        userRepository.save(sender);
        userRepository.save(recipient);
        transactionRepository.save(transaction);

        // Send notifications to both sender and receiver
        notificationService.notifyTransfer(sender.getId(), recipient.getId(), amount);

        logger.info("Money transfer completed successfully. Transaction ID: {}", transaction.getId());
        return transaction;
    }

    /**
     * Adds funds to the authenticated user's wallet.
     * This operation creates a deposit transaction and updates the user's balance.
     *
     * @param amount the amount to deposit
     * @return the transaction record of the completed deposit
     * @throws IllegalArgumentException if the amount is invalid
     * @throws UserNotFoundException    if the user is not found
     * @throws AccessDeniedException    if the user is not authenticated
     */
    @Transactional
    public Transaction addFunds(Money amount) {
        logger.info("Processing fund addition request, amount: {}", amount);

        // Validate the amount
        if (amount == null || amount.isZero()) {
            logger.error("Invalid deposit amount: {}", amount);
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        // Get the authenticated user
        UserId userId = getCurrentUserId();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", userId);
                    return new UserNotFoundException(userId);
                });

        // Create the deposit transaction record
        Transaction transaction = Transaction.createDeposit(user.getId(), amount);

        // Update user balance
        user.creditBalance(amount);

        // Save all changes
        userRepository.save(user);
        transactionRepository.save(transaction);

        // Send deposit notification to user
        notificationService.notifyDeposit(user.getId(), amount);

        logger.info("Fund addition completed successfully. Transaction ID: {}, New balance: {}",
                transaction.getId(), user.getBalance());
        return transaction;
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
}