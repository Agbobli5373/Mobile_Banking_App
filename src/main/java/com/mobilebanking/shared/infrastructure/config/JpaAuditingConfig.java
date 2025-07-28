package com.mobilebanking.shared.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Database configuration for the application.
 * Configures transaction management and JPA auditing.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
public class JpaAuditingConfig {

    /**
     * Creates a JdbcTemplate bean for direct database access.
     * 
     * @param dataSource the configured data source
     * @return JdbcTemplate instance
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Provides an auditor aware implementation for JPA auditing.
     * This can be extended to use the authenticated user in the future.
     * 
     * @return AuditorAware implementation
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        // This can be extended to use SecurityContextHolder to get the current user
        return () -> Optional.of("system");
    }
}