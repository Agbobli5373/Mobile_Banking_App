package com.mobilebanking.health;

import org.springframework.boot.actuate.health.Health;import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for application readiness
 */
@Component
public class ApplicationReadinessIndicator implements HealthIndicator {

    private boolean applicationReady = false;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        this.applicationReady = true;
    }

    @Override
    public Health health() {
        if (applicationReady) {
            return Health.up()
                    .withDetail("status", "Application is ready")
                    .withDetail("startup", "completed")
                    .build();
        } else {
            return Health.down()
                    .withDetail("status", "Application is starting up")
                    .withDetail("startup", "in-progress")
                    .build();
        }
    }
}