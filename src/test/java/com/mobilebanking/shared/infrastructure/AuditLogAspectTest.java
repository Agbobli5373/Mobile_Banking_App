package com.mobilebanking.shared.infrastructure;

import com.mobilebanking.shared.domain.AuditActionType;
import com.mobilebanking.shared.domain.AuditEntityType;
import com.mobilebanking.shared.domain.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogAspectTest {

    @Mock
    private AuditService auditService;

    @Mock
    private SecurityContextUtils securityContextUtils;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private AuditLogAspect auditLogAspect;

    @BeforeEach
    void setUp() {
        auditLogAspect = new AuditLogAspect(auditService, securityContextUtils);
    }

    @Test
    void auditControllerMethod_SuccessfulExecution_ShouldLogAction() throws Throwable {
        // Given
        Method method = TestController.class.getMethod("getBalance");
        String userId = "user-123";
        String ipAddress = "192.168.1.1";
        Object expectedResult = "Success";

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(securityContextUtils.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(securityContextUtils.getClientIpAddress()).thenReturn(Optional.of(ipAddress));
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // When
        Object result = auditLogAspect.auditControllerMethod(joinPoint);

        // Then
        assertEquals(expectedResult, result);
        verify(auditService).logUserAction(
            eq(userId),
            eq(AuditActionType.TRANSACTION_VIEWED),
            eq(AuditEntityType.SYSTEM),
            isNull(),
            eq("Successfully executed getBalance"),
            eq(ipAddress)
        );
    }

    @Test
    void auditControllerMethod_ExceptionThrown_ShouldLogError() throws Throwable {
        // Given
        Method method = TestController.class.getMethod("transferMoney");
        String userId = "user-123";
        String ipAddress = "192.168.1.1";
        RuntimeException expectedException = new RuntimeException("Transfer failed");

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(securityContextUtils.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(securityContextUtils.getClientIpAddress()).thenReturn(Optional.of(ipAddress));
        when(joinPoint.proceed()).thenThrow(expectedException);

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            auditLogAspect.auditControllerMethod(joinPoint);
        });
        assertEquals(expectedException, exception);

        verify(auditService).logUserAction(
            eq(userId),
            eq(AuditActionType.SYSTEM_ERROR),
            any(AuditEntityType.class),
            isNull(),
            contains("Error"),
            eq(ipAddress)
        );
    }

    @Test
    void auditControllerMethod_NoAuthenticatedUser_ShouldNotLogUserAction() throws Throwable {
        // Given
        Method method = TestController.class.getMethod("getBalance");
        Object expectedResult = "Success";

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(securityContextUtils.getCurrentUserId()).thenReturn(Optional.empty());
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // When
        Object result = auditLogAspect.auditControllerMethod(joinPoint);

        // Then
        assertEquals(expectedResult, result);
        verify(auditService, never()).logUserAction(
            anyString(),
            any(AuditActionType.class),
            any(AuditEntityType.class),
            anyString(),
            anyString(),
            anyString()
        );
    }

    @Test
    void auditControllerMethod_ExceptionWithNoUser_ShouldLogSystemError() throws Throwable {
        // Given
        Method method = TestController.class.getMethod("transferMoney");
        RuntimeException expectedException = new RuntimeException("Transfer failed");

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(securityContextUtils.getCurrentUserId()).thenReturn(Optional.empty());
        when(joinPoint.proceed()).thenThrow(expectedException);

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            auditLogAspect.auditControllerMethod(joinPoint);
        });
        assertEquals(expectedException, exception);

        verify(auditService).logSystemAction(
            eq(AuditActionType.SYSTEM_ERROR),
            any(AuditEntityType.class),
            isNull(),
            contains("Error")
        );
    }

    // Test controller class for method signature testing
    static class TestController {
        @GetMapping("/balance")
        public String getBalance() {
            return "Balance: $100";
        }

        @PostMapping("/transfer")
        public String transferMoney() {
            return "Transfer successful";
        }
    }
}