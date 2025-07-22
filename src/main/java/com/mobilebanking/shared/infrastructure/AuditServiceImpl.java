package com.mobilebanking.shared.infrastructure;

import com.mobilebanking.shared.domain.AuditActionType;import com.mobilebanking.shared.domain.AuditEntityType;
import com.mobilebanking.shared.domain.AuditLog;
import com.mobilebanking.shared.domain.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of the AuditService interface.
 * Uses a separate transaction to ensure audit logs are created even if the main transaction fails.
 */
@Service
public class AuditServiceImpl implements AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    /**
     * Log a user action with full context.
     * Uses REQUIRES_NEW propagation to ensure audit logs are committed even if the calling transaction fails.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUserAction(String userId, AuditActionType actionType, AuditEntityType entityType, 
                             String entityId, String details, String ipAddress) {
        AuditLog auditLog = AuditLog.create(
            userId,
            actionType.toString(),
            entityType.toString(),
            entityId,
            details,
            ipAddress
        );
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Log a user action with minimal context.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUserAction(String userId, AuditActionType actionType, String details) {
        AuditLog auditLog = AuditLog.create(
            userId,
            actionType.toString(),
            AuditEntityType.USER.toString(),
            userId,
            details,
            null
        );
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Log a system action (no user context).
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSystemAction(AuditActionType actionType, AuditEntityType entityType, 
                               String entityId, String details) {
        AuditLog auditLog = AuditLog.createSystemLog(
            actionType.toString(),
            entityType.toString(),
            entityId,
            details
        );
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Get audit logs for a specific user.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getUserAuditLogs(String userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get audit logs for a specific user with pagination.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getUserAuditLogs(String userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }
    
    /**
     * Get audit logs by action type.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByActionType(AuditActionType actionType) {
        return auditLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType.toString());
    }
    
    /**
     * Get audit logs for a specific entity.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getEntityAuditLogs(AuditEntityType entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            entityType.toString(), entityId);
    }
    
    /**
     * Get audit logs within a date range.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByDateRange(startDate, endDate);
    }
}