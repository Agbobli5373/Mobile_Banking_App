package com.mobilebanking.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for observability features
 */
@SpringBootTest
@ActiveProfiles("test")
class ObservabilityIntegrationTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private ObservabilityService observabilityService;

    @Autowired
    private Counter userRegistrationCounter;

    @Autowired
    private Timer walletOperationTimer;

    @Test
    void shouldHaveObservabilityBeansConfigured() {
        assertThat(meterRegistry).isNotNull();
        assertThat(observabilityService).isNotNull();
        assertThat(userRegistrationCounter).isNotNull();
        assertThat(walletOperationTimer).isNotNull();
    }

    @Test
    void shouldRecordUserRegistrationMetric() {
        double initialCount = userRegistrationCounter.count();

        observabilityService.recordUserRegistration("test-user-123");

        assertThat(userRegistrationCounter.count()).isEqualTo(initialCount + 1);
    }

    @Test
    void shouldRecordTransactionMetric() {
        observabilityService.recordTransaction("txn-123", "DEPOSIT", 100.0);

        // Verify that transaction counter was created and incremented
        Counter transactionCounter = meterRegistry.find("mobile_banking_transactions_total")
                .tag("type", "DEPOSIT")
                .counter();

        assertThat(transactionCounter).isNotNull();
        assertThat(transactionCounter.count()).isGreaterThan(0);
    }

    @Test
    void shouldTimeWalletOperations() {
        observabilityService.recordWalletOperation("BALANCE_CHECK", "wallet-123", () -> {
            // Simulate some work
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertThat(walletOperationTimer.count()).isGreaterThan(0);
        assertThat(walletOperationTimer.totalTime(TimeUnit.MILLISECONDS)).isGreaterThan(0);
    }

    @Test
    void shouldLogStructuredInformation() {
        // This test verifies that structured logging doesn't throw exceptions
        observabilityService.logStructuredInfo("TEST_OPERATION", "Test details");

        // If we reach here without exceptions, the test passes
        assertThat(true).isTrue();
    }
}