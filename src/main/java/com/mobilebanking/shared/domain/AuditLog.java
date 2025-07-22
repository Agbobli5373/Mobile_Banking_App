package com.mobilebanking.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for tracking system operations and user actions.
 * Used for audit trail and compliance purposes.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default constructor for JPA
    protected AuditLog() {
    }

    // Constructor for creating new audit logs
    private AuditLog(String userId, String actionType, String entityType,
            String entityId, String details, String ipAddress) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.ipAddress = ipAddress;
        this.createdAt = LocalDateTime.now();
    }

    // Factory method for creating audit logs
    public static AuditLog create(String userId, String actionType, String entityType,
            String entityId, String details, String ipAddress) {
        return new AuditLog(userId, actionType, entityType, entityId, details, ipAddress);
    }

    // Factory method for system-generated logs (no user context)
    public static AuditLog createSystemLog(String actionType, String entityType,
            String entityId, String details) {
        return new AuditLog(null, actionType, entityType, entityId, details, null);
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getDetails() {
        return details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}