package com.mobilebanking.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
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
    Counter userRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("mobile_banking_user_registrations_total")
                .description("Total number of user registrations")
                .register(meterRegistry);
    }

    /**
     * Counter for successful transactions
     */
    @Bean
    Counter transactionSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("mobile_banking_transactions_success_total")
                .description("Total number of successful transactions")
                .register(meterRegistry);
    }

    /**
     * Counter for failed transactions
     */
    @Bean
    Counter transactionFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("mobile_banking_transactions_failure_total")
                .description("Total number of failed transactions")
                .register(meterRegistry);
    }

    /**
     * Timer for wallet operations
     */
    @Bean
    Timer walletOperationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("mobile_banking_wallet_operations_duration")
                .description("Duration of wallet operations")
                .register(meterRegistry);
    }

    /**
     * Timer for authentication operations
     */
    @Bean
    Timer authenticationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("mobile_banking_authentication_duration")
                .description("Duration of authentication operations")
                .register(meterRegistry);
    }

    /**
     * Counter for login attempts
     */
    @Bean
    Counter loginAttemptCounter(MeterRegistry meterRegistry) {
        return Counter.builder("mobile_banking_login_attempts_total")
                .description("Total number of login attempts")
                .tag("status", "unknown")
                .register(meterRegistry);
    }

    /**
     * Counter for successful logins
     */
    @Bean
    Counter loginSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("mobile_banking_login_success_total")
                .description("Total number of successful logins")
                .register(meterRegistry);
    }

    /**
     * Counter for failed logins
     */
    @Bean
    Counter loginFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("mobile_banking_login_failure_total")
                .description("Total number of failed logins")
                .register(meterRegistry);
    }

    /**
     * Timer for balance check operations
     */
    @Bean
    Timer balanceCheckTimer(MeterRegistry meterRegistry) {
        return Timer.builder("mobile_banking_balance_check_duration")
                .description("Duration of balance check operations")
                .register(meterRegistry);
    }
}