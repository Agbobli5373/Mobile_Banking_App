package com.mobilebanking.shared.infrastructure;

import com.mobilebanking.shared.domain.AuditActionType;
import com.mobilebanking.shared.domain.AuditEntityType;
import com.mobilebanking.shared.domain.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Aspect for automatic audit logging of controller methods.
 */
@Aspect
@Component
public class AuditLogAspect {

  private final AuditService auditService;
  private final SecurityContextUtils securityContextUtils;

  public AuditLogAspect(AuditService auditService, SecurityContextUtils securityContextUtils) {
    this.auditService = auditService;
    this.securityContextUtils = securityContextUtils;
  }

  /**
   * Intercept controller methods and log audit events.
   */
  @Around("@within(org.springframework.web.bind.annotation.RestController)")
  public Object auditControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    // Get method signature
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();

    // Determine action type based on HTTP method
    AuditActionType actionType = determineActionType(method);

    // Determine entity type based on controller path
    AuditEntityType entityType = determineEntityType(method.getDeclaringClass());

    // Get user ID from security context
    Optional<String> userIdOpt = securityContextUtils.getCurrentUserId();

    // Get client IP address
    Optional<String> ipAddressOpt = securityContextUtils.getClientIpAddress();

    // Execute the controller method
    Object result;
    try {
      result = joinPoint.proceed();

      // Log successful action
      if (userIdOpt.isPresent()) {
        auditService.logUserAction(
            userIdOpt.get(),
            actionType,
            entityType,
            null, // Entity ID not available generically
            "Successfully executed " + method.getName(),
            ipAddressOpt.orElse(null));
      }

      return result;
    } catch (Throwable ex) {
      // Log failed action
      if (userIdOpt.isPresent()) {
        auditService.logUserAction(
            userIdOpt.get(),
            AuditActionType.SYSTEM_ERROR,
            entityType,
            null,
            "Error executing " + method.getName() + ": " + ex.getMessage(),
            ipAddressOpt.orElse(null));
      } else {
        auditService.logSystemAction(
            AuditActionType.SYSTEM_ERROR,
            entityType,
            null,
            "Error executing " + method.getName() + ": " + ex.getMessage());
      }
      throw ex;
    }
  }

  /**
   * Determine the action type based on HTTP method annotations.
   */
  private AuditActionType determineActionType(Method method) {
    if (method.isAnnotationPresent(GetMapping.class)) {
      if (method.getName().contains("balance")) {
        return AuditActionType.BALANCE_CHECKED;
      } else if (method.getName().contains("transaction")) {
        return AuditActionType.TRANSACTION_HISTORY_VIEWED;
      } else {
        return AuditActionType.TRANSACTION_VIEWED;
      }
    } else if (method.isAnnotationPresent(PostMapping.class)) {
      if (method.getName().contains("login")) {
        return AuditActionType.USER_LOGIN;
      } else if (method.getName().contains("register")) {
        return AuditActionType.USER_REGISTERED;
      } else if (method.getName().contains("transfer")) {
        return AuditActionType.MONEY_TRANSFERRED;
      } else if (method.getName().contains("add") || method.getName().contains("deposit")) {
        return AuditActionType.FUNDS_ADDED;
      }
    } else if (method.isAnnotationPresent(PutMapping.class)) {
      if (method.getName().contains("transfer")) {
        return AuditActionType.MONEY_TRANSFERRED;
      }
    }

    // Default action type
    return AuditActionType.TRANSACTION_VIEWED;
  }

  /**
   * Determine the entity type based on controller class.
   */
  private AuditEntityType determineEntityType(Class<?> controllerClass) {
    String className = controllerClass.getSimpleName().toLowerCase();

    if (className.contains("auth")) {
      return AuditEntityType.AUTHENTICATION;
    } else if (className.contains("user")) {
      return AuditEntityType.USER;
    } else if (className.contains("wallet")) {
      return AuditEntityType.WALLET;
    } else if (className.contains("transaction")) {
      return AuditEntityType.TRANSACTION;
    }

    // Default entity type
    return AuditEntityType.SYSTEM;
  }
}