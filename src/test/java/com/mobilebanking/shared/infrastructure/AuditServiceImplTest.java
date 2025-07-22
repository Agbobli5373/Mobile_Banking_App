package com.mobilebanking.shared.infrastructure;

import com.mobilebanking.shared.domain.AuditActionType;
import com.mobilebanking.shared.domain.AuditEntityType;
import com.mobilebanking.shared.domain.AuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditServiceImpl auditService;

    @Captor
    private ArgumentCaptor<AuditLog> auditLogCaptor;

    @BeforeEach
    void setUp() {
        auditService = new AuditServiceImpl(auditLogRepository);
    }

    @Test
    void logUserAction_WithFullContext_ShouldSaveAuditLog() {
        // Given
        String userId = "user-123";
        AuditActionType actionType = AuditActionType.MONEY_TRANSFERRED;
        AuditEntityType entityType = AuditEntityType.TRANSACTION;
        String entityId = "tx-456";
        String details = "Transferred $100 to user-789";
        String ipAddress = "192.168.1.1";

        // When
        auditService.logUserAction(userId, actionType, entityType, entityId, details, ipAddress);

        // Then
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();

        assertNotNull(capturedLog.getId());
        assertEquals(userId, capturedLog.getUserId());
        assertEquals(actionType.toString(), capturedLog.getActionType());
        assertEquals(entityType.toString(), capturedLog.getEntityType());
        assertEquals(entityId, capturedLog.getEntityId());
        assertEquals(details, capturedLog.getDetails());
        assertEquals(ipAddress, capturedLog.getIpAddress());
        assertNotNull(capturedLog.getCreatedAt());
    }

    @Test
    void logUserAction_WithMinimalContext_ShouldSaveAuditLog() {
        // Given
        String userId = "user-123";
        AuditActionType actionType = AuditActionType.BALANCE_CHECKED;
        String details = "Checked balance";

        // When
        auditService.logUserAction(userId, actionType, details);

        // Then
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();

        assertNotNull(capturedLog.getId());
        assertEquals(userId, capturedLog.getUserId());
        assertEquals(actionType.toString(), capturedLog.getActionType());
        assertEquals(AuditEntityType.USER.toString(), capturedLog.getEntityType());
        assertEquals(userId, capturedLog.getEntityId());
        assertEquals(details, capturedLog.getDetails());
    }

    @Test
    void logSystemAction_ShouldSaveAuditLog() {
        // Given
        AuditActionType actionType = AuditActionType.SYSTEM_ERROR;
        AuditEntityType entityType = AuditEntityType.SYSTEM;
        String entityId = "system-123";
        String details = "Database connection error";

        // When
        auditService.logSystemAction(actionType, entityType, entityId, details);

        // Then
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();

        assertNotNull(capturedLog.getId());
        assertEquals(null, capturedLog.getUserId());
        assertEquals(actionType.toString(), capturedLog.getActionType());
        assertEquals(entityType.toString(), capturedLog.getEntityType());
        assertEquals(entityId, capturedLog.getEntityId());
        assertEquals(details, capturedLog.getDetails());
    }

    @Test
    void getUserAuditLogs_ShouldReturnUserLogs() {
        // Given
        String userId = "user-123";
        List<AuditLog> expectedLogs = Arrays.asList(
                createMockAuditLog("log-1", userId),
                createMockAuditLog("log-2", userId));
        when(auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(expectedLogs);

        // When
        List<AuditLog> result = auditService.getUserAuditLogs(userId);

        // Then
        assertEquals(expectedLogs.size(), result.size());
        verify(auditLogRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void getUserAuditLogs_WithPagination_ShouldReturnPagedUserLogs() {
        // Given
        String userId = "user-123";
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> logs = Arrays.asList(
                createMockAuditLog("log-1", userId),
                createMockAuditLog("log-2", userId));
        Page<AuditLog> expectedPage = new PageImpl<>(logs, pageable, logs.size());
        when(auditLogRepository.findByUserId(userId, pageable)).thenReturn(expectedPage);

        // When
        Page<AuditLog> result = auditService.getUserAuditLogs(userId, pageable);

        // Then
        assertEquals(expectedPage.getTotalElements(), result.getTotalElements());
        verify(auditLogRepository).findByUserId(userId, pageable);
    }

    @Test
    void getAuditLogsByActionType_ShouldReturnActionTypeLogs() {
        // Given
        AuditActionType actionType = AuditActionType.MONEY_TRANSFERRED;
        List<AuditLog> expectedLogs = Arrays.asList(
                createMockAuditLog("log-1", "user-1"),
                createMockAuditLog("log-2", "user-2"));
        when(auditLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType.toString()))
                .thenReturn(expectedLogs);

        // When
        List<AuditLog> result = auditService.getAuditLogsByActionType(actionType);

        // Then
        assertEquals(expectedLogs.size(), result.size());
        verify(auditLogRepository).findByActionTypeOrderByCreatedAtDesc(actionType.toString());
    }

    @Test
    void getEntityAuditLogs_ShouldReturnEntityLogs() {
        // Given
        AuditEntityType entityType = AuditEntityType.TRANSACTION;
        String entityId = "tx-123";
        List<AuditLog> expectedLogs = Arrays.asList(
                createMockAuditLog("log-1", "user-1"),
                createMockAuditLog("log-2", "user-2"));
        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                entityType.toString(), entityId)).thenReturn(expectedLogs);

        // When
        List<AuditLog> result = auditService.getEntityAuditLogs(entityType, entityId);

        // Then
        assertEquals(expectedLogs.size(), result.size());
        verify(auditLogRepository).findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                entityType.toString(), entityId);
    }

    @Test
    void getAuditLogsByDateRange_ShouldReturnLogsInDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<AuditLog> expectedLogs = Arrays.asList(
                createMockAuditLog("log-1", "user-1"),
                createMockAuditLog("log-2", "user-2"));
        when(auditLogRepository.findByDateRange(startDate, endDate)).thenReturn(expectedLogs);

        // When
        List<AuditLog> result = auditService.getAuditLogsByDateRange(startDate, endDate);

        // Then
        assertEquals(expectedLogs.size(), result.size());
        verify(auditLogRepository).findByDateRange(startDate, endDate);
    }

    private AuditLog createMockAuditLog(String id, String userId) {
        AuditLog log = mock(AuditLog.class);
        return log;
    }
}