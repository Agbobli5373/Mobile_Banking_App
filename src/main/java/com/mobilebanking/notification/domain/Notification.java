package com.mobilebanking.notification.domain;

import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.shared.domain.Money;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Notification domain entity representing a notification sent to a user.
 * Immutable value object that contains notification details.
 */
public final class Notification {
    private final NotificationId id;
    private final UserId userId;
    private final String title;
    private final String message;
    private final NotificationType type;
    private final LocalDateTime timestamp;
    private final boolean read;

    private Notification(NotificationId id, UserId userId, String title, String message,
            NotificationType type, LocalDateTime timestamp, boolean read) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.read = read;
    }

    /**
     * Creates a new transfer notification for the sender.
     */
    public static Notification createTransferSent(UserId senderId, UserId receiverId, Money amount) {
        return new Notification(
                NotificationId.generate(),
                senderId,
                "Money Sent",
                String.format("You sent $%s to user %s", amount.toString(), receiverId.asString()),
                NotificationType.TRANSFER_SENT,
                LocalDateTime.now(),
                false);
    }

    /**
     * Creates a new transfer notification for the receiver.
     */
    public static Notification createTransferReceived(UserId receiverId, UserId senderId, Money amount) {
        return new Notification(
                NotificationId.generate(),
                receiverId,
                "Money Received",
                String.format("You received $%s from user %s", amount.toString(), senderId.asString()),
                NotificationType.TRANSFER_RECEIVED,
                LocalDateTime.now(),
                false);
    }

    /**
     * Creates a new deposit notification.
     */
    public static Notification createDepositNotification(UserId userId, Money amount) {
        return new Notification(
                NotificationId.generate(),
                userId,
                "Funds Added",
                String.format("$%s has been added to your wallet", amount.toString()),
                NotificationType.DEPOSIT,
                LocalDateTime.now(),
                false);
    }

    /**
     * Marks this notification as read.
     */
    public Notification markAsRead() {
        return new Notification(id, userId, title, message, type, timestamp, true);
    }

    // Getters
    public NotificationId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return read;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Notification that = (Notification) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Notification{id=%s, userId=%s, title='%s', type=%s, timestamp=%s, read=%s}",
                id, userId, title, type, timestamp, read);
    }
}