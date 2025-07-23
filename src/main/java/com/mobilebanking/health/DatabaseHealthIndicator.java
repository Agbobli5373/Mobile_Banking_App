package com.mobilebanking.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Custom health indicator for database connectivity
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                return Health.up()
                        .withDetail("database", "Connected")
                        .withDetail("status", "Connection successful")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "Disconnected")
                        .withDetail("status", "Connection validation failed")
                        .build();
            }
        } catch (SQLException e) {
            return Health.down()
                    .withDetail("database", "Error")
                    .withDetail("status", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}