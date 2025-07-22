package com.mobilebanking.shared.infrastructure;

import com.mobilebanking.shared.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for audit log operations.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    /**
     * Find audit logs by user ID.
     */
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find audit logs by user ID with pagination.
     */
    Page<AuditLog> findByUserId(String userId, Pageable pageable);

    /**
     * Find audit logs by action type.
     */
    List<AuditLog> findByActionTypeOrderByCreatedAtDesc(String actionType);

    /**
     * Find audit logs by entity type and entity ID.
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);

    /**
     * Find audit logs within a date range.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit logs by user ID and action type.
     */
    List<AuditLog> findByUserIdAndActionTypeOrderByCreatedAtDesc(String userId, String actionType);

    /**
     * Find audit logs by user ID and entity type.
     */
    List<AuditLog> findByUserIdAndEntityTypeOrderByCreatedAtDesc(String userId, String entityType);
}