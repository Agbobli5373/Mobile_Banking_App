package com.mobilebanking.notification.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for notification summary data.
 */
@Schema(description = "Notification summary containing counts and overview")
public class NotificationSummaryResponse {

    @Schema(description = "Total number of notifications", example = "15")
    private int totalCount;

    @Schema(description = "Number of unread notifications", example = "3")
    private int unreadCount;

    // Default constructor for JSON deserialization
    public NotificationSummaryResponse() {
    }

    public NotificationSummaryResponse(int totalCount, int unreadCount) {
        this.totalCount = totalCount;
        this.unreadCount = unreadCount;
    }

    // Getters and setters
    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    @Override
    public String toString() {
        return String.format("NotificationSummaryResponse{totalCount=%d, unreadCount=%d}",
                totalCount, unreadCount);
    }
}