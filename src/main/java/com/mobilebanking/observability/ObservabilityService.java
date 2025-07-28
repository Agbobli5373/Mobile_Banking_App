package com.mobilebanking.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class demonstrating observability features usage
 */
@Service
public class ObservabilityService {

    private static final Logger logger = LoggerFactory.getLogger(ObservabilityService.class);

    private final Counter userRegistrationCounter;
    private final Timer walletOperationTimer;
    private final Timer authenticationTimer;
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Timer balanceCheckTimer;
    private final MeterRegistry meterRegistry;

    @Autowired
    public ObservabilityService(Counter userRegistrationCounter,
            Timer walletOperationTimer,
            Timer authenticationTimer,
            Counter loginSuccessCounter,
            Counter loginFailureCounter,
            Timer balanceCheckTimer,
            MeterRegistry meterRegistry) {
        this.userRegistrationCounter = userRegistrationCounter;
        this.walletOperationTimer = walletOperationTimer;
        this.authenticationTimer = authenticationTimer;
        this.loginSuccessCounter = loginSuccessCounter;
        this.loginFailureCounter = loginFailureCounter;
        this.balanceCheckTimer = balanceCheckTimer;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Record user registration metric
     */
    @NewSpan("user-registration")
    public void recordUserRegistration(@SpanTag("userId") String userId) {
        userRegistrationCounter.increment();

        MDC.put("operation", "user-registration");
        MDC.put("userId", userId);

        logger.info("User registration recorded for user: {}", userId);

        MDC.remove("operation");
        MDC.remove("userId");
    }

    /**
     * Record transaction metric
     */
    @NewSpan("transaction")
    public void recordTransaction(@SpanTag("transactionId") String transactionId,
            @SpanTag("type") String type,
            @SpanTag("amount") double amount) {
        // Create a counter with tags for transaction type
        Counter.builder("mobile_banking_transactions_total")
                .description("Total number of transactions")
                .tag("type", type)
                .register(meterRegistry)
                .increment();

        MDC.put("operation", "transaction");
        MDC.put("transactionId", transactionId);
        MDC.put("transactionType", type);
        MDC.put("amount", String.valueOf(amount));

        logger.info("Transaction recorded: {} of type {} with amount {}",
                transactionId, type, amount);

        MDC.remove("operation");
        MDC.remove("transactionId");
        MDC.remove("transactionType");
        MDC.remove("amount");
    }

    /**
     * Time wallet operations
     */
    @NewSpan("wallet-operation")
    public void recordWalletOperation(@SpanTag("operation") String operation,
            @SpanTag("walletId") String walletId,
            Runnable walletOperation) {
        Timer.Sample sample = Timer.start(meterRegistry);

        MDC.put("operation", "wallet-operation");
        MDC.put("walletOperation", operation);
        MDC.put("walletId", walletId);

        try {
            logger.info("Starting wallet operation: {} for wallet: {}", operation, walletId);
            walletOperation.run();
            logger.info("Completed wallet operation: {} for wallet: {}", operation, walletId);
        } catch (Exception e) {
            logger.error("Failed wallet operation: {} for wallet: {}", operation, walletId, e);
            throw e;
        } finally {
            sample.stop(walletOperationTimer);
            MDC.remove("operation");
            MDC.remove("walletOperation");
            MDC.remove("walletId");
        }
    }

    /**
     * Time authentication operations
     */
    @NewSpan("authentication")
    public void recordAuthentication(@SpanTag("username") String username,
            @SpanTag("success") boolean success) {
        Timer.Sample sample = Timer.start(meterRegistry);

        MDC.put("operation", "authentication");
        MDC.put("username", username);
        MDC.put("success", String.valueOf(success));

        try {
            if (success) {
                loginSuccessCounter.increment();
                logger.info("Successful authentication for user: {}", username);
            } else {
                loginFailureCounter.increment();
                logger.warn("Failed authentication attempt for user: {}", username);
            }
        } finally {
            sample.stop(authenticationTimer);
            MDC.remove("operation");
            MDC.remove("username");
            MDC.remove("success");
        }
    }

    /**
     * Time balance check operations
     */
    @NewSpan("balance-check")
    public void recordBalanceCheck(@SpanTag("userId") String userId, Runnable balanceCheckOperation) {
        Timer.Sample sample = Timer.start(meterRegistry);

        MDC.put("operation", "balance-check");
        MDC.put("userId", userId);

        try {
            logger.info("Starting balance check for user: {}", userId);
            balanceCheckOperation.run();
            logger.info("Completed balance check for user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed balance check for user: {}", userId, e);
            throw e;
        } finally {
            sample.stop(balanceCheckTimer);
            MDC.remove("operation");
            MDC.remove("userId");
        }
    }

    /**
     * Log structured information with correlation
     */
    public void logStructuredInfo(String operation, String details) {
        MDC.put("operation", operation);
        MDC.put("details", details);

        logger.info("Structured log entry for operation: {} with details: {}", operation, details);

        MDC.remove("operation");
        MDC.remove("details");
    }
}