package com.mobilebanking.notification.domain;

import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.shared.domain.Money;
import java.util.List;

/**
 * Domain service interface for managing notifications.
 * Provides extensible notification functionality for different types of alerts.
 */
public interface NotificationService {

    /**
     * Sends a notification to a user.
     *
     * @param notification the notification to send
     */
    void sendNotification(Notification notification);

    /**
     * Retrieves all notifications for a specific user.
     *
     * @param userId the user ID
     * @return list of notifications for the user, ordered by timestamp descending
     */
    List<Notification> getNotificationsForUser(UserId userId);

    /**
     * Retrieves unread notifications for a specific user.
     *
     * @param userId the user ID
     * @return list of unread notifications for the user, ordered by timestamp
     *         descending
     */
    List<Notification> getUnreadNotificationsForUser(UserId userId);

    /**
     * Marks a notification as read.
     *
     * @param notificationId the notification ID
     * @param userId         the user ID (for security validation)
     */
    void markNotificationAsRead(NotificationId notificationId, UserId userId);

    /**
     * Sends transfer notifications to both sender and receiver.
     *
     * @param senderId   the sender's user ID
     * @param receiverId the receiver's user ID
     * @param amount     the transfer amount
     */
    void notifyTransfer(UserId senderId, UserId receiverId, Money amount);

    /**
     * Sends a deposit notification to the user.
     *
     * @param userId the user ID
     * @param amount the deposit amount
     */
    void notifyDeposit(UserId userId, Money amount);

    /**
     * Gets the count of unread notifications for a user.
     *
     * @param userId the user ID
     * @return the count of unread notifications
     */
    int getUnreadNotificationCount(UserId userId);
}