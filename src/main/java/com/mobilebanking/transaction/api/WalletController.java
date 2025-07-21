package com.mobilebanking.transaction.api;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.exception.InsufficientFundsException;
import com.mobilebanking.shared.domain.exception.UserNotFoundException;
import com.mobilebanking.transaction.api.dto.BalanceResponse;
import com.mobilebanking.transaction.api.dto.DepositRequest;
import com.mobilebanking.transaction.api.dto.DepositResponse;
import com.mobilebanking.transaction.api.dto.TransferRequest;
import com.mobilebanking.transaction.api.dto.TransferResponse;
import com.mobilebanking.transaction.application.WalletService;
import com.mobilebanking.transaction.domain.Transaction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * Endpoint for retrieving the authenticated user's balance.
     *
     * @return balance response with current balance
     */
    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance() {
        logger.info("Received balance request for authenticated user");

        try {
            Money balance = walletService.getBalance();
            logger.info("Balance retrieved successfully");
            return ResponseEntity.ok(BalanceResponse.success(balance));
        } catch (UserNotFoundException e) {
            logger.warn("Balance retrieval failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            logger.warn("Unauthorized balance access attempt: {}", e.getMessage());
            return ResponseEntity.status(403).body(BalanceResponse.failure("Access denied"));
        } catch (Exception e) {
            logger.error("Unexpected error during balance retrieval", e);
            return ResponseEntity.internalServerError()
                    .body(BalanceResponse.failure("An unexpected error occurred"));
        }
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

        try {
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

        } catch (UserNotFoundException e) {
            logger.warn("Transfer failed - recipient not found: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(TransferResponse.failure("Recipient not found: " + request.getRecipientPhone()));

        } catch (InsufficientFundsException e) {
            logger.warn("Transfer failed - insufficient funds: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(TransferResponse.failure("Insufficient funds for this transfer"));

        } catch (IllegalArgumentException e) {
            logger.warn("Transfer failed - invalid request: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(TransferResponse.failure(e.getMessage()));

        } catch (AccessDeniedException e) {
            logger.warn("Unauthorized transfer attempt: {}", e.getMessage());
            return ResponseEntity.status(403)
                    .body(TransferResponse.failure("Access denied"));

        } catch (Exception e) {
            logger.error("Unexpected error during money transfer", e);
            return ResponseEntity.internalServerError()
                    .body(TransferResponse.failure("An unexpected error occurred"));
        }
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

        try {
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

        } catch (IllegalArgumentException e) {
            logger.warn("Deposit failed - invalid request: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(DepositResponse.failure(e.getMessage()));

        } catch (UserNotFoundException e) {
            logger.warn("Deposit failed - user not found: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(DepositResponse.failure("User not found"));

        } catch (AccessDeniedException e) {
            logger.warn("Unauthorized deposit attempt: {}", e.getMessage());
            return ResponseEntity.status(403)
                    .body(DepositResponse.failure("Access denied"));

        } catch (Exception e) {
            logger.error("Unexpected error during fund addition", e);
            return ResponseEntity.internalServerError()
                    .body(DepositResponse.failure("An unexpected error occurred"));
        }
    }
}