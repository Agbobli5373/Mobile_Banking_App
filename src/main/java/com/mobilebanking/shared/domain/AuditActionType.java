package com.mobilebanking.shared.domain;

/**
 * Enum defining standard audit action types for consistency across the
 * application.
 */
public enum AuditActionType {
    // Authentication actions
    USER_REGISTERED,
    USER_LOGIN,
    USER_LOGOUT,

    // Wallet actions
    BALANCE_CHECKED,
    MONEY_TRANSFERRED,
    FUNDS_ADDED,

    // Transaction actions
    TRANSACTION_VIEWED,
    TRANSACTION_HISTORY_VIEWED,

    // System actions
    SYSTEM_ERROR,
    CONFIGURATION_CHANGED;

    @Override
    public String toString() {
        return name();
    }
}