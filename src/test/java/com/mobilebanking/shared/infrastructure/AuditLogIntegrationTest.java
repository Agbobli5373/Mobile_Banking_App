package com.mobilebanking.shared.infrastructure;

import com.mobilebanking.shared.domain.AuditActionType;
import com.mobilebanking.shared.domain.AuditEntityType;
import com.mobilebanking.shared.domain.AuditLog;
import com.mobilebanking.shared.domain.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Transactional
class AuditLogIntegrationTest {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void clearAuditLogs() {
        auditLogRepository.deleteAll();
    }

    @Test
    void logUserAction_ShouldPersistAuditLog() {
        // Given
        String userId = "test-user-id";
        AuditActionType actionType = AuditActionType.MONEY_TRANSFERRED;
        AuditEntityType entityType = AuditEntityType.TRANSACTION;
        String entityId = "test-transaction-id";
        String details = "Test transfer details";
        String ipAddress = "127.0.0.1";

        // When
        auditService.logUserAction(userId, actionType, entityType, entityId, details, ipAddress);

        // Then
        List<AuditLog> logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        assertFalse(logs.isEmpty());
        AuditLog log = logs.get(0);
        assertEquals(userId, log.getUserId());
        assertEquals(actionType.toString(), log.getActionType());
        assertEquals(entityType.toString(), log.getEntityType());
        assertEquals(entityId, log.getEntityId());
        assertEquals(details, log.getDetails());
        assertEquals(ipAddress, log.getIpAddress());
    }

    @Test
    void logSystemAction_ShouldPersistAuditLog() {
        // Given
        AuditActionType actionType = AuditActionType.SYSTEM_ERROR;
        AuditEntityType entityType = AuditEntityType.SYSTEM;
        String entityId = "system-component-id";
        String details = "Test system error details";

        // When
        auditService.logSystemAction(actionType, entityType, entityId, details);

        // Then
        List<AuditLog> logs = auditLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType.toString());
        
        assertFalse(logs.isEmpty());
        AuditLog log = logs.get(0);
        assertNull(log.getUserId());
        assertEquals(actionType.toString(), log.getActionType());
        assertEquals(entityType.toString(), log.getEntityType());
        assertEquals(entityId, log.getEntityId());
        assertEquals(details, log.getDetails());
    }

    @Test
    void getUserAuditLogs_ShouldReturnUserLogs() {
        // Given
        String userId = "test-user-id";
        auditService.logUserAction(userId, AuditActionType.USER_LOGIN, "User login successful");
        auditService.logUserAction(userId, AuditActionType.BALANCE_CHECKED, "Balance check");

        // When
        List<AuditLog> logs = auditService.getUserAuditLogs(userId);

        // Then
        assertEquals(2, logs.size());
        assertEquals(AuditActionType.BALANCE_CHECKED.toString(), logs.get(0).getActionType());
        assertEquals(AuditActionType.USER_LOGIN.toString(), logs.get(1).getActionType());
    }

    @Test
    void getAuditLogsByActionType_ShouldReturnActionTypeLogs() {
        // Given
        String userId1 = "user-1";
        String userId2 = "user-2";
        AuditActionType actionType = AuditActionType.MONEY_TRANSFERRED;
        
        auditService.logUserAction(userId1, actionType, AuditEntityType.TRANSACTION, "tx-1", "Transfer 1", null);
        auditService.logUserAction(userId2, actionType, AuditEntityType.TRANSACTION, "tx-2", "Transfer 2", null);
        auditService.logUserAction(userId1, AuditActionType.BALANCE_CHECKED, "Balance check");

        // When
        List<AuditLog> logs = auditService.getAuditLogsByActionType(actionType);

        // Then
        assertEquals(2, logs.size());
        assertTrue(logs.stream().allMatch(log -> log.getActionType().equals(actionType.toString())));
    }

    @Test
    void getEntityAuditLogs_ShouldReturnEntityLogs() {
        // Given
        AuditEntityType entityType = AuditEntityType.TRANSACTION;
        String entityId = "tx-123";
        
        auditService.logUserAction("user-1", AuditActionType.MONEY_TRANSFERRED, entityType, entityId, "Transfer", null);
        auditService.logUserAction("user-2", AuditActionType.TRANSACTION_VIEWED, entityType, entityId, "View", null);
        auditService.logUserAction("user-1", AuditActionType.BALANCE_CHECKED, AuditEntityType.WALLET, "wallet-1", "Check", null);

        // When
        List<AuditLog> logs = auditService.getEntityAuditLogs(entityType, entityId);

        // Then
        assertEquals(2, logs.size());
        assertTrue(logs.stream().allMatch(log -> 
            log.getEntityType().equals(entityType.toString()) && log.getEntityId().equals(entityId)));
    }

    @Test
    void getAuditLogsByDateRange_ShouldReturnLogsInDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusMinutes(5);
        LocalDateTime endDate = LocalDateTime.now().plusMinutes(5);
        
        auditService.logUserAction("user-1", AuditActionType.USER_LOGIN, "Login");
        auditService.logUserAction("user-2", AuditActionType.BALANCE_CHECKED, "Balance");

        // When
        List<AuditLog> logs = auditService.getAuditLogsByDateRange(startDate, endDate);

        // Then
        assertEquals(2, logs.size());
    }

    @Test
    void getUserAuditLogs_WithPagination_ShouldReturnPagedResults() {
        // Given
        String userId = "test-user-id";
        for (int i = 0; i < 10; i++) {
            auditService.logUserAction(userId, AuditActionType.USER_LOGIN, "Login attempt " + i);
        }

        // When
        var page1 = auditService.getUserAuditLogs(userId, PageRequest.of(0, 5));
        var page2 = auditService.getUserAuditLogs(userId, PageRequest.of(1, 5));

        // Then
        assertEquals(5, page1.getContent().size());
        assertEquals(5, page2.getContent().size());
        assertEquals(10, page1.getTotalElements());
        assertEquals(10, page2.getTotalElements());
        
        // Verify different content on different pages
        assertNotEquals(
            page1.getContent().get(0).getId(),
            page2.getContent().get(0).getId()
        );
    }
}