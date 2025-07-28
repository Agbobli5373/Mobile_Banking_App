package com.mobilebanking.aspect;

import com.mobilebanking.config.TracingConfiguration.TracingHelper;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.stereotype.Component;

/**
 * Aspect for adding distributed tracing to business operations
 */
@Aspect
@Component
@ConditionalOnEnabledTracing
public class TracingAspect {

    private static final Logger logger = LoggerFactory.getLogger(TracingAspect.class);
    private final TracingHelper tracingHelper;

    public TracingAspect(TracingHelper tracingHelper) {
        this.tracingHelper = tracingHelper;
    }

    /**
     * Add tracing to all service layer methods
     */
    @Around("execution(* com.mobilebanking.service.*.*(..))")
    public Object traceServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        tracingHelper.addCustomAttribute("service.class", className);
        tracingHelper.addCustomAttribute("service.method", methodName);

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            tracingHelper.addCustomAttribute("operation.duration_ms", String.valueOf(duration));
            tracingHelper.addCustomAttribute("operation.status", "success");
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            tracingHelper.addCustomAttribute("operation.duration_ms", String.valueOf(duration));
            tracingHelper.addCustomAttribute("operation.status", "error");
            tracingHelper.addCustomAttribute("error.message", e.getMessage());
            tracingHelper.addCustomAttribute("error.type", e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Add tracing to repository layer methods
     */
    @Around("execution(* com.mobilebanking.repository.*.*(..))")
    public Object traceRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        tracingHelper.addCustomAttribute("repository.class", className);
        tracingHelper.addCustomAttribute("repository.method", methodName);

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            tracingHelper.addCustomAttribute("db.operation.duration_ms", String.valueOf(duration));
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            tracingHelper.addCustomAttribute("db.operation.duration_ms", String.valueOf(duration));
            tracingHelper.addCustomAttribute("db.error.message", e.getMessage());
            throw e;
        }
    }
}