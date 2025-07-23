package com.mobilebanking.notification.api.dto;

import com.mobilebanking.notification.domain.Notification;
import com.mobilebanking.notification.domain.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Response DTO for notification data.
 */
@Schema(description = "Notification response containing notification details")
public class NotificationResponse {

    @Schema(description = "Unique notification identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Notification title", example = "Money Sent")
    private String title;

    @Schema(description = "Notification message", example = "You sent $50.00 to user 456")
    private String message;

    @Schema(description = "Type of notification", example = "TRANSFER_SENT")
    private NotificationType type;

    @Schema(description = "Timestamp when notification was created", example = "2025-07-23T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Whether the notification has been read", example = "false")
    private boolean read;

    // Default constructor for JSON deserialization
    public NotificationResponse() {
    }

    public NotificationResponse(String id, String title, String message, NotificationType type,
            LocalDateTime timestamp, boolean read) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.read = read;
    }

    /**
     * Creates a NotificationResponse from a domain Notification.
     */
    public static NotificationResponse fromDomain(Notification notification) {
        return new NotificationResponse(
                notification.getId().asString(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getTimestamp(),
                notification.isRead());
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return String.format("NotificationResponse{id='%s', title='%s', type=%s, timestamp=%s, read=%s}",
                id, title, type, timestamp, read);
    }
}