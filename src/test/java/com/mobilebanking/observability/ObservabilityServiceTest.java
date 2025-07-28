package com.mobilebanking.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ObservabilityServiceTest {

    private ObservabilityService observabilityService;
    private MeterRegistry meterRegistry;
    private Counter userRegistrationCounter;
    private Timer walletOperationTimer;
    private Timer authenticationTimer;
    private Counter loginSuccessCounter;
    private Counter loginFailureCounter;
    private Timer balanceCheckTimer;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();

        userRegistrationCounter = Counter.builder("mobile_banking_user_registrations_total")
                .description("Total number of user registrations")
                .register(meterRegistry);

        walletOperationTimer = Timer.builder("mobile_banking_wallet_operations_duration")
                .description("Duration of wallet operations")
                .register(meterRegistry);

        authenticationTimer = Timer.builder("mobile_banking_authentication_duration")
                .description("Duration of authentication operations")
                .register(meterRegistry);

        loginSuccessCounter = Counter.builder("mobile_banking_login_success_total")
                .description("Total number of successful logins")
                .register(meterRegistry);

        loginFailureCounter = Counter.builder("mobile_banking_login_failure_total")
                .description("Total number of failed logins")
                .register(meterRegistry);

        balanceCheckTimer = Timer.builder("mobile_banking_balance_check_duration")
                .description("Duration of balance check operations")
                .register(meterRegistry);

        observabilityService = new ObservabilityService(
                userRegistrationCounter,
                walletOperationTimer,
                authenticationTimer,
                loginSuccessCounter,
                loginFailureCounter,
                balanceCheckTimer,
                meterRegistry);
    }

    @Test
    void testRecordUserRegistration() {
        // Given
        String userId = "user123";
        double initialCount = userRegistrationCounter.count();

        // When
        observabilityService.recordUserRegistration(userId);

        // Then
        assertEquals(initialCount + 1, userRegistrationCounter.count());
    }

    @Test
    void testRecordTransaction() {
        // Given
        String transactionId = "tx123";
        String type = "transfer";
        double amount = 100.0;

        // When
        observabilityService.recordTransaction(transactionId, type, amount);

        // Then
        Counter transactionCounter = meterRegistry.find("mobile_banking_transactions_total")
                .tag("type", type)
                .counter();
        assertNotNull(transactionCounter);
        assertEquals(1.0, transactionCounter.count());
    }

    @Test
    void testRecordWalletOperation() {
        // Given
        String operation = "balance_check";
        String walletId = "wallet123";
        Runnable walletOperation = () -> {
            // Simulate some work
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        long initialCount = walletOperationTimer.count();

        // When
        observabilityService.recordWalletOperation(operation, walletId, walletOperation);

        // Then
        assertEquals(initialCount + 1, walletOperationTimer.count());
        assertTrue(walletOperationTimer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) > 0);
    }

    @Test
    void testRecordAuthentication() {
        // Given
        String username = "testuser";
        boolean success = true;
        long initialCount = authenticationTimer.count();

        // When
        observabilityService.recordAuthentication(username, success);

        // Then
        assertEquals(initialCount + 1, authenticationTimer.count());
    }

    @Test
    void testLogStructuredInfo() {
        // Given
        String operation = "test_operation";
        String details = "test details";

        // When/Then - This should not throw any exceptions
        assertDoesNotThrow(() -> {
            observabilityService.logStructuredInfo(operation, details);
        });
    }
}