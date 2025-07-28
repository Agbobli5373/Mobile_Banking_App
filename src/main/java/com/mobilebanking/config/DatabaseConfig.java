package com.mobilebanking.config;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.db.PostgreSQLDatabaseMetrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Database configuration with connection pool monitoring
 */
@Configuration
public class DatabaseConfig {

    /**
     * PostgreSQL metrics for monitoring database performance
     * Only enabled when PostgreSQL driver is available and kubernetes profile is
     * active
     */
    @Bean
    @ConditionalOnClass(name = "org.postgresql.Driver")
    @Profile("kubernetes")
    @ConditionalOnProperty(name = "management.metrics.export.prometheus.enabled", havingValue = "true", matchIfMissing = true)
    PostgreSQLDatabaseMetrics postgreSQLDatabaseMetrics(DataSource dataSource, MeterRegistry meterRegistry) {
        return new PostgreSQLDatabaseMetrics(dataSource, "mobile_banking_db");
    }

    /**
     * HikariCP connection pool metrics configuration
     * This method is called automatically by Spring Boot to configure HikariCP
     * metrics
     */
    @Bean
    @ConditionalOnClass(HikariDataSource.class)
    @ConditionalOnProperty(name = "management.metrics.export.prometheus.enabled", havingValue = "true", matchIfMissing = true)
    public HikariDataSourceMetricsTracker hikariMetricsTracker(DataSource dataSource, MeterRegistry meterRegistry) {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            hikariDataSource.setMetricRegistry(meterRegistry);
            return new HikariDataSourceMetricsTracker();
        }
        return null;
    }

    /**
     * Simple tracker class to indicate HikariCP metrics are configured
     */
    public static class HikariDataSourceMetricsTracker {
        // Marker class to indicate metrics are configured
    }
}