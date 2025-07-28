package com.mobilebanking.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Configuration for distributed tracing correlation
 */
@Configuration
public class TracingConfig {

    private static final Logger logger = LoggerFactory.getLogger(TracingConfig.class);

    /**
     * Filter to add trace correlation IDs to MDC for structured logging
     * This works with Spring Boot's auto-configured OpenTelemetry tracing
     */
    @Bean
    OncePerRequestFilter traceCorrelationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain) throws ServletException, IOException {

                String correlationId = request.getHeader("X-Correlation-ID");
                if (correlationId == null || correlationId.isEmpty()) {
                    correlationId = UUID.randomUUID().toString();
                }

                try {
                    // Add correlation ID to MDC
                    MDC.put("correlationId", correlationId);
                    MDC.put("requestUri", request.getRequestURI());
                    MDC.put("requestMethod", request.getMethod());

                    // Add correlation ID to response header
                    response.setHeader("X-Correlation-ID", correlationId);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Processing request " + request.getMethod() + " " +
                                request.getRequestURI() + " with correlation ID: " + correlationId);
                    }

                    filterChain.doFilter(request, response);
                } finally {
                    // Clear MDC to prevent memory leaks
                    MDC.clear();
                }
            }
        };
    }
}