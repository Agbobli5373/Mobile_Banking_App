package com.mobilebanking.transaction.application;

import com.mobilebanking.shared.api.Audited;
import com.mobilebanking.shared.domain.AuditActionType;
import com.mobilebanking.shared.domain.AuditEntityType;
import com.mobilebanking.shared.domain.AuditService;
import com.mobilebanking.shared.infrastructure.SecurityContextUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Example service showing how to use audit logging with both annotations and
 * direct service calls.
 */
@Service
public class AuditedTransactionService {

    private final AuditService auditService;
    private final SecurityContextUtils securityContextUtils;

    public AuditedTransactionService(AuditService auditService, SecurityContextUtils securityContextUtils) {
        this.auditService = auditService;
        this.securityContextUtils = securityContextUtils;
    }

    /**
     * Example of using @Audited annotation for automatic audit logging.
     */
    @Audited(action = AuditActionType.MONEY_TRANSFERRED, entity = AuditEntityType.TRANSACTION, description = "Money transfer between users")
    @Transactional
    public void transferMoney(String senderId, String receiverId, double amount) {
        // Implementation of money transfer logic

        // The audit log will be created automatically by the AuditedAspect
    }

    /**
     * Example of manual audit logging for more complex scenarios.
     */
    @Transactional
    public void addFunds(String userId, double amount) {
        // Implementation of fund addition logic

        // Manual audit logging with more context
        securityContextUtils.getCurrentUserId().ifPresent(currentUserId -> {
            String ipAddress = securityContextUtils.getClientIpAddress().orElse(null);

            auditService.logUserAction(
                    currentUserId,
                    AuditActionType.FUNDS_ADDED,
                    AuditEntityType.WALLET,
                    userId,
                    String.format("Added %.2f to user %s", amount, userId),
                    ipAddress);
        });
    }

    /**
     * Example of system action logging.
     */
    @Transactional
    public void processScheduledTransactions() {
        // Implementation of scheduled transaction processing

        // Log system action (no user context)
        auditService.logSystemAction(
                AuditActionType.TRANSACTION_VIEWED,
                AuditEntityType.SYSTEM,
                "scheduler",
                "Processed scheduled transactions");
    }
}