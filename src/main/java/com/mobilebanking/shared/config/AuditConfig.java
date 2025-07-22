package com.mobilebanking.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration class to enable AspectJ for audit logging.
 */
@Configuration
@EnableAspectJAutoProxy
public class AuditConfig {
    // Configuration is handled by annotations
}