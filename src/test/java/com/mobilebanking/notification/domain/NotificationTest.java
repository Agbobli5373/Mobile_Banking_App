package com.mobilebanking.notification.domain;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Notification domain entity.
 */
class NotificationTest {

    @Test
    void shouldCreateTransferSentNotification() {
        // Given
        UserId senderId = UserId.generate();
        UserId receiverId = UserId.generate();
        Money amount = Money.of(100.00);

        // When
        Notification notification = Notification.createTransferSent(senderId, receiverId, amount);

        // Then
        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getUserId()).isEqualTo(senderId);
        assertThat(notification.getTitle()).isEqualTo("Money Sent");
        assertThat(notification.getMessage()).contains("$100.00");
        assertThat(notification.getMessage()).contains(receiverId.asString());
        assertThat(notification.getType()).isEqualTo(NotificationType.TRANSFER_SENT);
        assertThat(notification.getTimestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(notification.isRead()).isFalse();
    }

    @Test
    void shouldCreateTransferReceivedNotification() {
        // Given
        UserId senderId = UserId.generate();
        UserId receiverId = UserId.generate();
        Money amount = Money.of(50.00);

        // When
        Notification notification = Notification.createTransferReceived(receiverId, senderId, amount);

        // Then
        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getUserId()).isEqualTo(receiverId);
        assertThat(notification.getTitle()).isEqualTo("Money Received");
        assertThat(notification.getMessage()).contains("$50.00");
        assertThat(notification.getMessage()).contains(senderId.asString());
        assertThat(notification.getType()).isEqualTo(NotificationType.TRANSFER_RECEIVED);
        assertThat(notification.getTimestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(notification.isRead()).isFalse();
    }

    @Test
    void shouldCreateDepositNotification() {
        // Given
        UserId userId = UserId.generate();
        Money amount = Money.of(200.00);

        // When
        Notification notification = Notification.createDepositNotification(userId, amount);

        // Then
        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getTitle()).isEqualTo("Funds Added");
        assertThat(notification.getMessage()).contains("$200.00");
        assertThat(notification.getType()).isEqualTo(NotificationType.DEPOSIT);
        assertThat(notification.getTimestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(notification.isRead()).isFalse();
    }

    @Test
    void shouldMarkNotificationAsRead() {
        // Given
        UserId userId = UserId.generate();
        Money amount = Money.of(100.00);
        Notification originalNotification = Notification.createDepositNotification(userId, amount);

        // When
        Notification readNotification = originalNotification.markAsRead();

        // Then
        assertThat(readNotification.getId()).isEqualTo(originalNotification.getId());
        assertThat(readNotification.getUserId()).isEqualTo(originalNotification.getUserId());
        assertThat(readNotification.getTitle()).isEqualTo(originalNotification.getTitle());
        assertThat(readNotification.getMessage()).isEqualTo(originalNotification.getMessage());
        assertThat(readNotification.getType()).isEqualTo(originalNotification.getType());
        assertThat(readNotification.getTimestamp()).isEqualTo(originalNotification.getTimestamp());
        assertThat(readNotification.isRead()).isTrue();
        assertThat(originalNotification.isRead()).isFalse(); // Original should remain unchanged
    }

    @Test
    void shouldHaveEqualityBasedOnId() {
        // Given
        UserId userId = UserId.generate();
        Money amount = Money.of(100.00);
        Notification notification1 = Notification.createDepositNotification(userId, amount);
        Notification notification2 = notification1.markAsRead();

        // When & Then
        assertThat(notification1).isEqualTo(notification2);
        assertThat(notification1.hashCode()).isEqualTo(notification2.hashCode());
    }

    @Test
    void shouldHaveProperToString() {
        // Given
        UserId userId = UserId.generate();
        Money amount = Money.of(100.00);
        Notification notification = Notification.createDepositNotification(userId, amount);

        // When
        String toString = notification.toString();

        // Then
        assertThat(toString).contains("Notification{");
        assertThat(toString).contains("id=" + notification.getId());
        assertThat(toString).contains("userId=" + userId);
        assertThat(toString).contains("title='Funds Added'");
        assertThat(toString).contains("type=DEPOSIT");
        assertThat(toString).contains("read=false");
    }
}