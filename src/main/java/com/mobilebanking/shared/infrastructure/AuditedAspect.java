package com.mobilebanking.shared.infrastructure;

import com.mobilebanking.shared.api.Audited;
import com.mobilebanking.shared.domain.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Aspect for handling methods annotated with @Audited.
 */
@Aspect
@Component
public class AuditedAspect {

    private final AuditService auditService;
    private final SecurityContextUtils securityContextUtils;

    public AuditedAspect(AuditService auditService, SecurityContextUtils securityContextUtils) {
        this.auditService = auditService;
        this.securityContextUtils = securityContextUtils;
    }

    /**
     * Intercept methods annotated with @Audited and log audit events.
     */
    @Around("@annotation(com.mobilebanking.shared.api.Audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get method signature
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Get annotation
        Audited audited = method.getAnnotation(Audited.class);

        // Get user ID from security context
        Optional<String> userIdOpt = securityContextUtils.getCurrentUserId();

        // Get client IP address
        Optional<String> ipAddressOpt = securityContextUtils.getClientIpAddress();

        // Prepare description
        String description = audited.description().isEmpty()
                ? "Executed " + method.getName()
                : audited.description();

        // Execute the method
        Object result;
        try {
            result = joinPoint.proceed();

            // Log successful action
            if (userIdOpt.isPresent()) {
                auditService.logUserAction(
                        userIdOpt.get(),
                        audited.action(),
                        audited.entity(),
                        null, // Entity ID not available generically
                        description,
                        ipAddressOpt.orElse(null));
            } else {
                auditService.logSystemAction(
                        audited.action(),
                        audited.entity(),
                        null,
                        description);
            }

            return result;
        } catch (Throwable ex) {
            // Log failed action
            if (userIdOpt.isPresent()) {
                auditService.logUserAction(
                        userIdOpt.get(),
                        audited.action(),
                        audited.entity(),
                        null,
                        description + " failed: " + ex.getMessage(),
                        ipAddressOpt.orElse(null));
            } else {
                auditService.logSystemAction(
                        audited.action(),
                        audited.entity(),
                        null,
                        description + " failed: " + ex.getMessage());
            }
            throw ex;
        }
    }
}