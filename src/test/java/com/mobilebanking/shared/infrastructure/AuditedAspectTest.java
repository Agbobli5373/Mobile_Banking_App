package com.mobilebanking.shared.infrastructure;

import com.mobilebanking.shared.api.Audited;import com.mobilebanking.shared.domain.AuditActionType;
import com.mobilebanking.shared.domain.AuditEntityType;
import com.mobilebanking.shared.domain.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditedAspectTest {

    @Mock
    private AuditService auditService;

    @Mock
    private SecurityContextUtils securityContextUtils;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private AuditedAspect auditedAspect;

    @BeforeEach
    void setUp() {
        auditedAspect = new AuditedAspect(auditService, securityContextUtils);
    }

    @Test
    void auditMethod_SuccessfulExecution_ShouldLogAction() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("transferMoney");
        String userId = "user-123";
        String ipAddress = "192.168.1.1";
        Object expectedResult = "Success";

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(securityContextUtils.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(securityContextUtils.getClientIpAddress()).thenReturn(Optional.of(ipAddress));
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // When
        Object result = auditedAspect.auditMethod(joinPoint);

        // Then
        assertEquals(expectedResult, result);
        verify(auditService).logUserAction(
            eq(userId),
            eq(AuditActionType.MONEY_TRANSFERRED),
            eq(AuditEntityType.TRANSACTION),
            isNull(),
            eq("Transfer money between accounts"),
            eq(ipAddress)
        );
    }

    @Test
    void auditMethod_ExceptionThrown_ShouldLogError() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("checkBalance");
        String userId = "user-123";
        String ipAddress = "192.168.1.1";
        RuntimeException expectedException = new RuntimeException("Balance check failed");

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(securityContextUtils.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(securityContextUtils.getClientIpAddress()).thenReturn(Optional.of(ipAddress));
        when(joinPoint.proceed()).thenThrow(expectedException);

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            auditedAspect.auditMethod(joinPoint);
        });
        assertEquals(expectedException, exception);

        verify(auditService).logUserAction(
            eq(userId),
            eq(AuditActionType.BALANCE_CHECKED),
            eq(AuditEntityType.WALLET),
            isNull(),
            contains("failed"),
            eq(ipAddress)
        );
    }

    @Test
    void auditMethod_NoAuthenticatedUser_ShouldLogSystemAction() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("transferMoney");
        Object expectedResult = "Success";

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(securityContextUtils.getCurrentUserId()).thenReturn(Optional.empty());
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // When
        Object result = auditedAspect.auditMethod(joinPoint);

        // Then
        assertEquals(expectedResult, result);
        verify(auditService).logSystemAction(
            eq(AuditActionType.MONEY_TRANSFERRED),
            eq(AuditEntityType.TRANSACTION),
            isNull(),
            eq("Transfer money between accounts")
        );
    }

    @Test
    void auditMethod_ExceptionWithNoUser_ShouldLogSystemError() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("checkBalance");
        RuntimeException expectedException = new RuntimeException("Balance check failed");

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(securityContextUtils.getCurrentUserId()).thenReturn(Optional.empty());
        when(joinPoint.proceed()).thenThrow(expectedException);

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            auditedAspect.auditMethod(joinPoint);
        });
        assertEquals(expectedException, exception);

        verify(auditService).logSystemAction(
            eq(AuditActionType.BALANCE_CHECKED),
            eq(AuditEntityType.WALLET),
            isNull(),
            contains("failed")
        );
    }

    // Test service class for method signature testing
    static class TestService {
        @Audited(
            action = AuditActionType.MONEY_TRANSFERRED,
            entity = AuditEntityType.TRANSACTION,
            description = "Transfer money between accounts"
        )
        public String transferMoney() {
            return "Transfer successful";
        }

        @Audited(
            action = AuditActionType.BALANCE_CHECKED,
            entity = AuditEntityType.WALLET
        )
        public String checkBalance() {
            return "Balance: $100";
        }
    }
}