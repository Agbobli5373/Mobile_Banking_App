package com.mobilebanking.transaction.api;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.exception.UserNotFoundException;
import com.mobilebanking.transaction.api.dto.BalanceResponse;
import com.mobilebanking.transaction.application.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for wallet-related endpoints.
 * Handles balance retrieval and other wallet operations.
 */
@RestController
@RequestMapping("/api/wallet")
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
}