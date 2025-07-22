package com.mobilebanking.shared.api;

import com.mobilebanking.shared.domain.AuditActionType;
import com.mobilebanking.shared.domain.AuditEntityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for explicitly marking methods that should be audited.
 * This provides more control than the automatic aspect-based approach.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /**
     * The action type for the audit log.
     */
    AuditActionType action();

    /**
     * The entity type for the audit log.
     */
    AuditEntityType entity();

    /**
     * Optional description for the audit log.
     */
    String description() default "";
}