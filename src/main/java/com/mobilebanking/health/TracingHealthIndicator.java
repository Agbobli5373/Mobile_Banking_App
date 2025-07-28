package com.mobilebanking.health;

import io.micrometer.tracing.Tracer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for distributed tracing functionality
 */
@Component
public class TracingHealthIndicator implements HealthIndicator {

    private final Tracer tracer;

    public TracingHealthIndicator(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Health health() {
        try {
            // Test tracing functionality by creating a test span
            var span = tracer.nextSpan()
                    .name("health-check-test")
                    .tag("component", "health-indicator")
                    .start();

            try {
                // Check if we can get trace context
                var traceContext = span.context();
                if (traceContext != null && traceContext.traceId() != null) {
                    return Health.up()
                            .withDetail("tracing", "Distributed tracing operational")
                            .withDetail("status", "Tracer available")
                            .withDetail("traceId", traceContext.traceId())
                            .build();
                } else {
                    return Health.down()
                            .withDetail("tracing", "Trace context unavailable")
                            .withDetail("status", "Tracing context error")
                            .build();
                }
            } finally {
                span.end();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("tracing", "Tracing service error")
                    .withDetail("status", "Tracer unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}