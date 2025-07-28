package com.mobilebanking.health;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
/**
 * Custom health indicator for metrics collection functionality
 */
@Component
public class MetricsHealthIndicator implements HealthIndicator {

    private final MeterRegistry meterRegistry;

    public MetricsHealthIndicator(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Health health() {
        try {
            // Test metrics functionality by creating a test counter
            var testCounter = meterRegistry.counter("health.check.test", "component", "health-indicator");
            testCounter.increment();
            
            // Check if the counter was registered and incremented
            double count = testCounter.count();
            if (count > 0) {
                // Get some basic metrics about the registry
                int meterCount = meterRegistry.getMeters().size();
                
                return Health.up()
                        .withDetail("metrics", "Metrics collection operational")
                        .withDetail("status", "MeterRegistry available")
                        .withDetail("registeredMeters", meterCount)
                        .withDetail("testCounterValue", count)
                        .build();
            } else {
                return Health.down()
                        .withDetail("metrics", "Metrics increment failed")
                        .withDetail("status", "Metrics collection error")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("metrics", "Metrics service error")
                    .withDetail("status", "MeterRegistry unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}