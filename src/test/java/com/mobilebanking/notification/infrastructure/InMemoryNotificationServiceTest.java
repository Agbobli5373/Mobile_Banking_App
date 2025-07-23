package com.mobilebanking.notification.infrastructure;

import com.mobilebanking.notification.domain.Notification;
import com.mobilebanking.notification.domain.NotificationId;
import com.mobilebanking.notification.domain.NotificationType;
import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for InMemoryNotificationService.
 */
class InMemoryNotificationServiceTest {

    private InMemoryNotificationService notificationService;
    private UserId userId1;
    private UserId userId2;

    @BeforeEach
    void setUp() {
        notificationService = new InMemoryNotificationService();
        userId1 = UserId.generate();
        userId2 = UserId.generate();
    }

    @Test
    void shouldSendAndRetrieveNotification() {
        // Given
        Money amount = Money.of(100.00);
        Notification notification = Notification.createDepositNotification(userId1, amount);

        // When
        notificationService.sendNotification(notification);

        // Then
        List<Notification> notifications = notificationService.getNotificationsForUser(userId1);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0)).isEqualTo(notification);
    }

    @Test
    void shouldReturnEmptyListForUserWithNoNotifications() {
        // When
        List<Notification> notifications = notificationService.getNotificationsForUser(userId1);

        // Then
        assertThat(notifications).isEmpty();
    }

    @Test
    void shouldReturnNotificationsInDescendingOrderByTimestamp() throws InterruptedException {
        // Given
        Money amount = Money.of(100.00);
        Notification notification1 = Notification.createDepositNotification(userId1, amount);

        // Small delay to ensure different timestamps
        Thread.sleep(1);

        Notification notification2 = Notification.createDepositNotification(userId1, amount);

        // When
        notificationService.sendNotification(notification1);
        notificationService.sendNotification(notification2);

        // Then
        List<Notification> notifications = notificationService.getNotificationsForUser(userId1);
        assertThat(notifications).hasSize(2);
        assertThat(notifications.get(0).getTimestamp()).isAfter(notifications.get(1).getTimestamp());
    }

    @Test
    void shouldGetUnreadNotificationsOnly() {
        // Given
        Money amount = Money.of(100.00);
        Notification notification1 = Notification.createDepositNotification(userId1, amount);
        Notification notification2 = Notification.createDepositNotification(userId1, amount);

        notificationService.sendNotification(notification1);
        notificationService.sendNotification(notification2);

        // Mark one as read
        notificationService.markNotificationAsRead(notification1.getId(), userId1);

        // When
        List<Notification> unreadNotifications = notificationService.getUnreadNotificationsForUser(userId1);

        // Then
        assertThat(unreadNotifications).hasSize(1);
        assertThat(unreadNotifications.get(0).getId()).isEqualTo(notification2.getId());
        assertThat(unreadNotifications.get(0).isRead()).isFalse();
    }

    @Test
    void shouldMarkNotificationAsRead() {
        // Given
        Money amount = Money.of(100.00);
        Notification notification = Notification.createDepositNotification(userId1, amount);
        notificationService.sendNotification(notification);

        // When
        notificationService.markNotificationAsRead(notification.getId(), userId1);

        // Then
        List<Notification> notifications = notificationService.getNotificationsForUser(userId1);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).isRead()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenMarkingNonExistentNotificationAsRead() {
        // Given
        NotificationId nonExistentId = NotificationId.generate();

        // When & Then
        assertThatThrownBy(() -> notificationService.markNotificationAsRead(nonExistentId, userId1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notification not found");
    }

    @Test
    void shouldThrowExceptionWhenMarkingNotificationOfDifferentUser() {
        // Given
        Money amount = Money.of(100.00);
        Notification notification = Notification.createDepositNotification(userId1, amount);
        notificationService.sendNotification(notification);

        // When & Then
        assertThatThrownBy(() -> notificationService.markNotificationAsRead(notification.getId(), userId2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to the specified user");
    }

    @Test
    void shouldNotifyTransferToSenderAndReceiver() {
        // Given
        Money amount = Money.of(150.00);

        // When
        notificationService.notifyTransfer(userId1, userId2, amount);

        // Then
        List<Notification> senderNotifications = notificationService.getNotificationsForUser(userId1);
        List<Notification> receiverNotifications = notificationService.getNotificationsForUser(userId2);

        assertThat(senderNotifications).hasSize(1);
        assertThat(receiverNotifications).hasSize(1);

        Notification senderNotification = senderNotifications.get(0);
        assertThat(senderNotification.getType()).isEqualTo(NotificationType.TRANSFER_SENT);
        assertThat(senderNotification.getTitle()).isEqualTo("Money Sent");
        assertThat(senderNotification.getMessage()).contains("$150.00");

        Notification receiverNotification = receiverNotifications.get(0);
        assertThat(receiverNotification.getType()).isEqualTo(NotificationType.TRANSFER_RECEIVED);
        assertThat(receiverNotification.getTitle()).isEqualTo("Money Received");
        assertThat(receiverNotification.getMessage()).contains("$150.00");
    }

    @Test
    void shouldNotifyDeposit() {
        // Given
        Money amount = Money.of(200.00);

        // When
        notificationService.notifyDeposit(userId1, amount);

        // Then
        List<Notification> notifications = notificationService.getNotificationsForUser(userId1);
        assertThat(notifications).hasSize(1);

        Notification notification = notifications.get(0);
        assertThat(notification.getType()).isEqualTo(NotificationType.DEPOSIT);
        assertThat(notification.getTitle()).isEqualTo("Funds Added");
        assertThat(notification.getMessage()).contains("$200.00");
    }

@Test
    void shouldGetUnreadNotificationCount() {
        // Given
        Money amount = Money.of(100.00);
        Notification notification1 = Notification.createDepositNotification(userId1, amount);
        Notification notification2 = Notification.createDepositNotification(userId1, amount);
        Notification notification3 = Notification.createDepositNotification(userId1, amount);

        notificationService.sendNotification(notification1);
        notificationService.sendNotification(notification2);
        notificationService.sendNotification(notification3);

        // Mark one as read
        notificationService.markNotificationAsRead(notification1.getId(), userId1);

        // When
        int unreadCount = notificationService.getUnreadNotificationCount(userId1);

        // Then
        assertThat(unreadCount).isEqualTo(2);
    }

    @Test
    void shouldReturnZeroUnreadCountForUserWithNoNotifications() {
        // When
        int unreadCount = notificationService.getUnreadNotificationCount(userId1);

        // Then
        assertThat(unreadCount).isZero();
    }

    @Test
    void shouldIsolateNotificationsBetweenUsers() {
        // Given
        Money amount = Money.of(100.00);
        Notification notification1 = Notification.createDepositNotification(userId1, amount);
        Notification notification2 = Notification.createDepositNotification(userId2, amount);

        // When
        notificationService.sendNotification(notification1);
        notificationService.sendNotification(notification2);

        // Then
        List<Notification> user1Notifications = notificationService.getNotificationsForUser(userId1);
        List<Notification> user2Notifications = notificationService.getNotificationsForUser(userId2);

        assertThat(user1Notifications).hasSize(1);
        assertThat(user2Notifications).hasSize(1);
        assertThat(user1Notifications.get(0).getId()).isEqualTo(notification1.getId());
        assertThat(user2Notifications.get(0).getId()).isEqualTo(notification2.getId());
    }

    @Test
    void shouldClearAllNotifications() {
        // Given
        Money amount = Money.of(100.00);
        Notification notification1 = Notification.createDepositNotification(userId1, amount);
        Notification notification2 = Notification.createDepositNotification(userId2, amount);

        notificationService.sendNotification(notification1);
        notificationService.sendNotification(notification2);

        // When
        notificationService.clearAllNotifications();

        // Then
        assertThat(notificationService.getNotificationsForUser(userId1)).isEmpty();
        assertThat(notificationService.getNotificationsForUser(userId2)).isEmpty();
        assertThat(notificationService.getTotalNotificationCount()).isZero();
    }

    @Test
    void shouldGetTotalNotificationCount() {
        // Given
        Money amount = Money.of(100.00);
        Notification notification1 = Notification.createDepositNotification(userId1, amount);
        Notification notification2 = Notification.createDepositNotification(userId2, amount);

        // When
        notificationService.sendNotification(notification1);
        notificationService.sendNotification(notification2);

        // Then
        assertThat(notificationService.getTotalNotificationCount()).isEqualTo(2);
    }
}