package com.mobilebanking.notification.api;

import com.mobilebanking.notification.api.dto.NotificationResponse;
import com.mobilebanking.notification.api.dto.NotificationSummaryResponse;
import com.mobilebanking.notification.domain.Notification;
import com.mobilebanking.notification.domain.NotificationId;
import com.mobilebanking.notification.domain.NotificationService;
import com.mobilebanking.shared.domain.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for notification-related operations.
 * Provides endpoints for retrieving and managing user notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification management operations")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Retrieves all notifications for the authenticated user.
     */
    @GetMapping
    @Operation(summary = "Get all notifications", description = "Retrieves all notifications for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        UserId userId = getCurrentUserId();
        logger.info("Retrieving all notifications for user: {}", userId);

        List<Notification> notifications = notificationService.getNotificationsForUser(userId);
        List<NotificationResponse> response = notifications.stream()
                .map(NotificationResponse::fromDomain)
                .collect(Collectors.toList());

        logger.debug("Retrieved {} notifications for user: {}", response.size(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves unread notifications for the authenticated user.
     */
    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Retrieves unread notifications for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        UserId userId = getCurrentUserId();
        logger.info("Retrieving unread notifications for user: {}", userId);

        List<Notification> notifications = notificationService.getUnreadNotificationsForUser(userId);
        List<NotificationResponse> response = notifications.stream()
                .map(NotificationResponse::fromDomain)
                .toList();

        logger.debug("Retrieved {} unread notifications for user: {}", response.size(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets a summary of notifications including unread count.
     */
    @GetMapping("/summary")
    @Operation(summary = "Get notification summary", description = "Gets notification summary including unread count")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification summary retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<NotificationSummaryResponse> getNotificationSummary() {
        UserId userId = getCurrentUserId();
        logger.info("Retrieving notification summary for user: {}", userId);

        int unreadCount = notificationService.getUnreadNotificationCount(userId);
        int totalCount = notificationService.getNotificationsForUser(userId).size();

        NotificationSummaryResponse response = new NotificationSummaryResponse(totalCount, unreadCount);

        logger.debug("Notification summary for user {}: total={}, unread={}", userId, totalCount, unreadCount);
        return ResponseEntity.ok(response);
    }

    /**
     * Marks a specific notification as read.
     */
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid notification ID or notification doesn't belong to user"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> markNotificationAsRead(
            @Parameter(description = "Notification ID", required = true)
            @PathVariable String notificationId) {
        UserId userId = getCurrentUserId();
        logger.info("Marking notification as read: {} for user: {}", notificationId, userId);

        try {
            NotificationId notifId = NotificationId.fromString(notificationId);
            notificationService.markNotificationAsRead(notifId, userId);
            logger.debug("Notification marked as read successfully: {}", notificationId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request to mark notification as read: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

/**
     * Gets the current authenticated user's ID.
     *
     * @return the current user's ID
     * @throws AccessDeniedException if no user is authenticated
     */
    private UserId getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getName().equals("anonymousUser")) {
            logger.error("No authenticated user found");
            throw new AccessDeniedException("User not authenticated");
        }

        return UserId.fromString(authentication.getName());
    }
}