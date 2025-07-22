package com.mobilebanking.shared.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for audit logging operations.
 */
public interface AuditService {

    /**
     * Log a user action with full context.
     */
    void logUserAction(String userId, AuditActionType actionType, AuditEntityType entityType,
            String entityId, String details, String ipAddress);

    /**
     * Log a user action with minimal context.
     */
    void logUserAction(String userId, AuditActionType actionType, String details);

    /**
     * Log a system action (no user context).
     */
    void logSystemAction(AuditActionType actionType, AuditEntityType entityType,
            String entityId, String details);

    /**
     * Get audit logs for a specific user.
     */
    List<AuditLog> getUserAuditLogs(String userId);

    /**
     * Get audit logs for a specific user with pagination.
     */
    Page<AuditLog> getUserAuditLogs(String userId, Pageable pageable);

    /**
     * Get audit logs by action type.
     */
    List<AuditLog> getAuditLogsByActionType(AuditActionType actionType);

    /**
     * Get audit logs for a specific entity.
     */
    List<AuditLog> getEntityAuditLogs(AuditEntityType entityType, String entityId);

    /**
     * Get audit logs within a date range.
     */
    List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}