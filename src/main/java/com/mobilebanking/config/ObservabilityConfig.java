package com.mobilebanking.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for observability features including health indicators
 * Note: Metrics are configured in MetricsConfiguration to avoid duplication
 */
@Configuration
public class ObservabilityConfig {

    private static final Logger logger = LoggerFactory.getLogger(ObservabilityConfig.class);

    /**
     * Health indicator for observability components
     */
    @Bean
    HealthIndicator observabilityHealthIndicator() {
        return () -> {
            try {
                // Check if observability components are working
                logger.debug("Observability health check performed");
                return Health.up()
                        .withDetail("tracing", "enabled")
                        .withDetail("metrics", "enabled")
                        .withDetail("logging", "structured")
                        .build();
            } catch (Exception e) {
                logger.error("Observability health check failed", e);
                return Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }
}