package com.mobilebanking.shared.api;

import com.mobilebanking.shared.domain.AuditActionType;
import com.mobilebanking.shared.domain.AuditEntityType;
import com.mobilebanking.shared.domain.AuditLog;
import com.mobilebanking.shared.domain.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for audit log operations.
 * This is an example of how to use the audit logging system in a controller.
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/user/{userId}")
    @Audited(action = AuditActionType.TRANSACTION_HISTORY_VIEWED, entity = AuditEntityType.USER, description = "Admin viewed user audit logs")
    public ResponseEntity<List<AuditLog>> getUserAuditLogs(@PathVariable String userId) {
        List<AuditLog> logs = auditService.getUserAuditLogs(userId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/user/{userId}/paged")
    public ResponseEntity<Page<AuditLog>> getUserAuditLogsPaged(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AuditLog> logs = auditService.getUserAuditLogs(userId, pageRequest);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/action/{actionType}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByActionType(
            @PathVariable String actionType) {

        try {
            AuditActionType type = AuditActionType.valueOf(actionType);
            List<AuditLog> logs = auditService.getAuditLogsByActionType(type);
            return ResponseEntity.ok(logs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLog>> getEntityAuditLogs(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        
        try {
            AuditEntityType type = AuditEntityType.valueOf(entityType);
            List<AuditLog> logs = auditService.getEntityAuditLogs(type, entityId);
            return ResponseEntity.ok(logs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

@GetMapping("/date-range")
    public ResponseEntity<List<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<AuditLog> logs = auditService.getAuditLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(logs);
    }
}