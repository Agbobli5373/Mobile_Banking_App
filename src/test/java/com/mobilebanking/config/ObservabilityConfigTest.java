package com.mobilebanking.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ObservabilityConfigTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private Counter userRegistrationCounter;

    @Autowired
    private Timer walletOperationTimer;

    @Autowired
    private Timer authenticationTimer;

    @Autowired
    private OncePerRequestFilter correlationIdFilter;

    @Autowired
    private HealthIndicator observabilityHealthIndicator;

    @Test
    void testMeterRegistryIsConfigured() {
        assertNotNull(meterRegistry);
    }

    @Test
    void testUserRegistrationCounterIsConfigured() {
        assertNotNull(userRegistrationCounter);
        assertEquals("mobile_banking_user_registrations_total", userRegistrationCounter.getId().getName());
    }

    @Test
    void testWalletOperationTimerIsConfigured() {
        assertNotNull(walletOperationTimer);
        assertEquals("mobile_banking_wallet_operations_duration", walletOperationTimer.getId().getName());
    }

    @Test
    void testAuthenticationTimerIsConfigured() {
        assertNotNull(authenticationTimer);
        assertEquals("mobile_banking_authentication_duration", authenticationTimer.getId().getName());
    }

    @Test
    void testCorrelationIdFilterIsConfigured() {
        assertNotNull(correlationIdFilter);
    }

    @Test
    void testObservabilityHealthIndicatorIsConfigured() {
        assertNotNull(observabilityHealthIndicator);

        // Test health check
        var health = observabilityHealthIndicator.health();
        assertNotNull(health);
        assertEquals("UP", health.getStatus().getCode());
        assertTrue(health.getDetails().containsKey("tracing"));
        assertTrue(health.getDetails().containsKey("metrics"));
        assertTrue(health.getDetails().containsKey("logging"));
    }

    @Test
    void testPrometheusMetricsAreAvailable() {
        // Verify that Prometheus metrics are available
        assertNotNull(meterRegistry.find("jvm.memory.used").gauge());
        assertNotNull(meterRegistry.find("system.cpu.usage").gauge());
    }
}