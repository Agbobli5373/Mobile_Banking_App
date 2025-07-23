package com.mobilebanking.notification.infrastructure;

import com.mobilebanking.notification.domain.Notification;
import com.mobilebanking.notification.domain.NotificationId;
import com.mobilebanking.notification.domain.NotificationService;
import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * In-memory implementation of NotificationService.
 * Stores notifications in memory using thread-safe collections.
 * This implementation is suitable for development and testing environments.
 */
@Service
public class InMemoryNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryNotificationService.class);

    // Thread-safe storage for notifications
    private final ConcurrentHashMap<UserId, CopyOnWriteArrayList<Notification>> userNotifications;
    private final ConcurrentHashMap<NotificationId, Notification> notificationStore;

    public InMemoryNotificationService() {
        this.userNotifications = new ConcurrentHashMap<>();
        this.notificationStore = new ConcurrentHashMap<>();
    }

    @Override
    public void sendNotification(Notification notification) {
        logger.info("Sending notification: {} to user: {}", notification.getId(), notification.getUserId());

        // Store the notification
        notificationStore.put(notification.getId(), notification);

        // Add to user's notification list
        userNotifications.computeIfAbsent(notification.getUserId(), k -> new CopyOnWriteArrayList<>())
                .add(notification);

        logger.debug("Notification sent successfully: {}", notification.getId());
    }

    @Override
    public List<Notification> getNotificationsForUser(UserId userId) {
        logger.debug("Retrieving all notifications for user: {}", userId);

        List<Notification> notifications = userNotifications.getOrDefault(userId, new CopyOnWriteArrayList<>())
                .stream()
                .sorted((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp())) // Most recent first
                .collect(Collectors.toList());

        logger.debug("Found {} notifications for user: {}", notifications.size(), userId);
        return notifications;
    }

    @Override
    public List<Notification> getUnreadNotificationsForUser(UserId userId) {
        logger.debug("Retrieving unread notifications for user: {}", userId);

        List<Notification> unreadNotifications = userNotifications.getOrDefault(userId, new CopyOnWriteArrayList<>())
                .stream()
                .filter(notification -> !notification.isRead())
                .sorted((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp())) // Most recent first
                .collect(Collectors.toList());

        logger.debug("Found {} unread notifications for user: {}", unreadNotifications.size(), userId);
        return unreadNotifications;
    }

    @Override
    public void markNotificationAsRead(NotificationId notificationId, UserId userId) {
        logger.info("Marking notification as read: {} for user: {}", notificationId, userId);

        Notification notification = notificationStore.get(notificationId);
        if (notification == null) {
            logger.warn("Notification not found: {}", notificationId);
            throw new IllegalArgumentException("Notification not found: " + notificationId);
        }

        // Verify the notification belongs to the user
        if (!notification.getUserId().equals(userId)) {
            logger.warn("User {} attempted to mark notification {} belonging to user {}",
                    userId, notificationId, notification.getUserId());
            throw new IllegalArgumentException("Notification does not belong to the specified user");
        }

        // Mark as read
        Notification readNotification = notification.markAsRead();

        // Update storage
        notificationStore.put(notificationId, readNotification);

        // Update user's notification list
        CopyOnWriteArrayList<Notification> userNotifs = userNotifications.get(userId);
        if (userNotifs != null) {
            // Replace the notification in the list
            for (int i = 0; i < userNotifs.size(); i++) {
                if (userNotifs.get(i).getId().equals(notificationId)) {
                    userNotifs.set(i, readNotification);
                    break;
                }
            }
        }

        logger.debug("Notification marked as read: {}", notificationId);
    }

    @Override
    public void notifyTransfer(UserId senderId, UserId receiverId, Money amount) {
        logger.info("Creating transfer notifications for sender: {} and receiver: {}, amount: {}",
                senderId, receiverId, amount);

        // Create and send notification to sender
        Notification senderNotification = Notification.createTransferSent(senderId, receiverId, amount);
        sendNotification(senderNotification);

        // Create and send notification to receiver
        Notification receiverNotification = Notification.createTransferReceived(receiverId, senderId, amount);
        sendNotification(receiverNotification);

        logger.info("Transfer notifications sent successfully");
    }

    @Override
    public void notifyDeposit(UserId userId, Money amount) {
        logger.info("Creating deposit notification for user: {}, amount: {}", userId, amount);

        Notification depositNotification = Notification.createDepositNotification(userId, amount);
        sendNotification(depositNotification);

        logger.info("Deposit notification sent successfully");
    }

@Override
    public int getUnreadNotificationCount(UserId userId) {
        logger.debug("Getting unread notification count for user: {}", userId);

        int count = (int) userNotifications.getOrDefault(userId, new CopyOnWriteArrayList<>())
                .stream()
                .filter(notification -> !notification.isRead())
                .count();

        logger.debug("Unread notification count for user {}: {}", userId, count);
        return count;
    }

    /**
     * Clears all notifications for testing purposes.
     * This method should only be used in test environments.
     */
    public void clearAllNotifications() {
        logger.warn("Clearing all notifications - this should only be used in tests");
        userNotifications.clear();
        notificationStore.clear();
    }

    /**
     * Gets the total number of notifications in the system.
     * This method is primarily for testing and monitoring purposes.
     */
    public int getTotalNotificationCount() {
        return notificationStore.size();
    }
}