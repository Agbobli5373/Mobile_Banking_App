package com.mobilebanking.shared.domain;

/**
 * Enum defining standard entity types for audit logging.
 */
public enum AuditEntityType {
    USER,
    TRANSACTION,
    WALLET,
    AUTHENTICATION,
    SYSTEM;

    @Override
    public String toString() {
        return name();
    }
}