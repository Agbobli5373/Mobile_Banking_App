package com.mobilebanking.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for custom application metrics
 */
@Configuration
public class MetricsConfiguration {

    /**
     * Counter for user registrations
     */
    @Bean
    public Counter userRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("mobile_banking_user_registrations_total")
                .description("Total number of user registrations")
                .register(meterRegistry);
    }

    /**
     * Counter for successful transactions
     */
    @Bean
    public Counter transactionSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("mobile_banking_transactions_success_total")
                .description("Total number of successful transactions")
                .register(meterRegistry);
    }

    /**
     * Counter for failed transactions
     */
    @Bean
    public Counter transactionFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("mobile_banking_transactions_failure_total")
                .description("Total number of failed transactions")
                .register(meterRegistry);
    }

    /**
     * Timer for wallet operations
     */
    @Bean
    public Timer walletOperationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("mobile_banking_wallet_operations_duration")
                .description("Duration of wallet operations")
                .register(meterRegistry);
    }

    /**
     * Timer for authentication operations
     */
    @Bean
    public Timer authenticationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("mobile_banking_authentication_duration")
                .description("Duration of authentication operations")
                .register(meterRegistry);
    }

    /**
     * Counter for login attempts
     */
    @Bean
    public Counter loginAttemptCounter(MeterRegistry meterRegistry) {
        return Counter.builder("mobile_banking_login_attempts_total")
                .description("Total number of login attempts")
                .tag("status", "unknown")
                .register(meterRegistry);
    }
}