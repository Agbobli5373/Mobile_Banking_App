package com.mobilebanking.config;

import io.micrometer.tracing.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenTelemetry distributed tracing
 */
@Configuration
@ConditionalOnEnabledTracing
public class TracingConfiguration {

    /**
     * Configure trace propagators for distributed tracing
     * Supports W3C Trace Context, B3, and Jaeger propagation formats
     */
    @Bean
    public ContextPropagators contextPropagators() {
        return ContextPropagators.create(
                TextMapPropagator.composite(
                        W3CTraceContextPropagator.getInstance(),
                        B3Propagator.injectingSingleHeader(),
                        JaegerPropagator.getInstance()));
    }

    /**
     * Custom trace attributes for business operations
     */
    @Bean
    public TracingHelper tracingHelper(Tracer tracer) {
        return new TracingHelper(tracer);
    }

    /**
     * Helper class for adding custom trace attributes
     */
    public static class TracingHelper {
        private final Tracer tracer;

        public TracingHelper(Tracer tracer) {
            this.tracer = tracer;
        }

        public void addCustomAttribute(String key, String value) {
            if (tracer.currentSpan() != null) {
                tracer.currentSpan().tag(key, value);
            }
        }

        public void addUserContext(String userId) {
            addCustomAttribute("user.id", userId);
        }

        public void addTransactionContext(String transactionId, String transactionType) {
            addCustomAttribute("transaction.id", transactionId);
            addCustomAttribute("transaction.type", transactionType);
        }

        public void addWalletContext(String walletId) {
            addCustomAttribute("wallet.id", walletId);
        }
    }
}