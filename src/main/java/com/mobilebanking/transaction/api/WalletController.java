package com.mobilebanking.transaction.api;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.exception.UserNotFoundException;
import com.mobilebanking.transaction.api.dto.BalanceResponse;
import com.mobilebanking.transaction.api.dto.DepositRequest;
import com.mobilebanking.transaction.api.dto.DepositResponse;
import com.mobilebanking.transaction.api.dto.TransactionHistoryResponse;
import com.mobilebanking.transaction.api.dto.TransferRequest;
import com.mobilebanking.transaction.api.dto.TransferResponse;
import com.mobilebanking.transaction.application.TransactionQueryService;
import com.mobilebanking.transaction.application.WalletService;
import com.mobilebanking.transaction.domain.Transaction;
import com.mobilebanking.shared.domain.UserId;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for wallet-related endpoints.
 * Handles balance retrieval and other wallet operations.
 */
@RestController
@RequestMapping("/api/wallet")
@SecurityRequirement(name = "bearer-key")
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);
    private final WalletService walletService;
    private final TransactionQueryService transactionQueryService;

    public WalletController(WalletService walletService, TransactionQueryService transactionQueryService) {
        this.walletService = walletService;
        this.transactionQueryService = transactionQueryService;
    }

    /**
     * Endpoint for retrieving the authenticated user's balance.
     *
     * @return balance response with current balance
     */
    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance() {
        logger.info("Received balance request for authenticated user");

        Money balance = walletService.getBalance();
        logger.info("Balance retrieved successfully");
        return ResponseEntity.ok(BalanceResponse.success(balance));
    }

    /**
     * Endpoint for transferring money to another user.
     *
     * @param request the transfer request containing recipient phone and amount
     * @return transfer response with transaction details
     */
    @PostMapping("/send")
    public ResponseEntity<TransferResponse> transferMoney(@Valid @RequestBody TransferRequest request) {
        logger.info("Received money transfer request: {}", request);

        // Convert the amount to Money domain object
        Money transferAmount = Money.of(request.getAmount());

        // Process the transfer
        Transaction transaction = walletService.transferMoney(
                request.getRecipientPhone(),
                transferAmount);

        // Get the updated balance after transfer
        Money newBalance = walletService.getBalance();

        logger.info("Money transfer completed successfully. Transaction ID: {}", transaction.getId());

        // Return success response with transaction details
        return ResponseEntity.ok(TransferResponse.success(
                transaction.getId(),
                transaction.getAmount(),
                request.getRecipientPhone(),
                newBalance));
    }

    /**
     * Endpoint for adding funds to the authenticated user's wallet.
     *
     * @param request the deposit request containing the amount to add
     * @return deposit response with transaction details
     */
    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> addFunds(@Valid @RequestBody DepositRequest request) {
        logger.info("Received fund addition request: {}", request);

        // Convert the amount to Money domain object
        Money depositAmount = Money.of(request.getAmount());

        // Process the deposit
        Transaction transaction = walletService.addFunds(depositAmount);

        // Get the updated balance after deposit
        Money newBalance = walletService.getBalance();

        logger.info("Fund addition completed successfully. Transaction ID: {}", transaction.getId());

        // Return success response with transaction details
        return ResponseEntity.ok(DepositResponse.success(
                transaction.getId(),
                transaction.getAmount(),
                newBalance));
    }

    /**
     * Endpoint for retrieving the authenticated user's transaction history.
     * Returns all transactions where the user is either sender or receiver,
     * ordered by timestamp in descending order (most recent first).
     *
     * @return transaction history response with list of transactions
     */
    @GetMapping("/transactions")
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistory() {
        logger.info("Received transaction history request for authenticated user");

        // Get current user ID for transaction direction determination
        UserId currentUserId = getCurrentUserId();

        // Retrieve transaction history
        List<Transaction> transactions = transactionQueryService.getTransactionHistory();

        logger.info("Transaction history retrieved successfully. Found {} transactions", transactions.size());

        // Return success response with transaction details
        return ResponseEntity.ok(TransactionHistoryResponse.success(transactions, currentUserId));
    }

    /**
     * Endpoint for retrieving the authenticated user's transaction history with
     * pagination.
     * Returns a page of transactions where the user is either sender or receiver,
     * ordered by timestamp in descending order (most recent first).
     *
     * @param page page number (0-based)
     * @param size page size
     * @return paginated transaction history response
     */
    @GetMapping("/transactions/paged")
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistoryPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Received paginated transaction history request for authenticated user. Page: {}, Size: {}", page,
                size);

        try {
            // Get current user ID for transaction direction determination
            UserId currentUserId = getCurrentUserId();

            // Create pageable request (already sorted by timestamp desc in repository)
            Pageable pageable = PageRequest.of(page, size);

            // Retrieve paginated transaction history
            Page<Transaction> transactionsPage = transactionQueryService.getTransactionHistoryPaginated(pageable);

            logger.info("Paginated transaction history retrieved successfully. Page: {}/{}, Elements: {}/{}",
                    transactionsPage.getNumber(), transactionsPage.getTotalPages(),
                    transactionsPage.getNumberOfElements(), transactionsPage.getTotalElements());

            // Return success response with transaction details and pagination info
            return ResponseEntity.ok(TransactionHistoryResponse.success(transactionsPage, currentUserId));

        } catch (UserNotFoundException e) {
            logger.warn("Paginated transaction history retrieval failed - user not found: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(TransactionHistoryResponse.failure("User not found"));

        } catch (AccessDeniedException e) {
            logger.warn("Unauthorized paginated transaction history access attempt: {}", e.getMessage());
            return ResponseEntity.status(403)
                    .body(TransactionHistoryResponse.failure("Access denied"));

        } catch (Exception e) {
            logger.error("Unexpected error during paginated transaction history retrieval", e);
            return ResponseEntity.internalServerError()
                    .body(TransactionHistoryResponse.failure("An unexpected error occurred"));
        }
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