package com.mobilebanking.notification;

import com.mobilebanking.auth.application.LoginService;
import com.mobilebanking.auth.api.dto.LoginRequest;
import com.mobilebanking.auth.api.dto.LoginResponse;
import com.mobilebanking.notification.api.dto.NotificationResponse;
import com.mobilebanking.notification.api.dto.NotificationSummaryResponse;
import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.transaction.api.dto.TransferRequest;
import com.mobilebanking.user.application.UserRegistrationService;
import com.mobilebanking.user.api.dto.UserRegistrationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the notification system.
 * Tests the complete flow from money transfer to notification delivery.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class NotificationIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRegistrationService userRegistrationService;

        @Autowired
        private LoginService loginService;

        private String senderToken;
        private String receiverToken;
        private String senderPhone = "1234567890";
        private String receiverPhone = "0987654321";

        @BeforeEach
        void setUp() {
                // Register sender
                userRegistrationService.registerUser("John Sender", senderPhone, "1234");

                // Register receiver
                userRegistrationService.registerUser("Jane Receiver", receiverPhone, "5678");

                // Login both users to get tokens
                senderToken = loginService.login(senderPhone, "1234");
                receiverToken = loginService.login(receiverPhone, "5678");
        }

        @Test
        void shouldCreateNotificationsWhenMoneyIsTransferred() throws Exception {
                // Given - Add funds to sender's account first
                mockMvc.perform(post("/api/wallet/add-funds")
                                .header("Authorization", "Bearer " + senderToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"amount\": 200.00}")
                                .with(csrf()))
                                .andExpect(status().isOk());

                // Verify sender has deposit notification
                MvcResult senderNotificationsResult = mockMvc.perform(get("/api/notifications")
                                .header("Authorization", "Bearer " + senderToken))
                                .andExpect(status().isOk())
                                .andReturn();

                NotificationResponse[] senderNotifications = objectMapper.readValue(
                                senderNotificationsResult.getResponse().getContentAsString(),
                                NotificationResponse[].class);
                assertThat(senderNotifications).hasSize(1);
                assertThat(senderNotifications[0].getType().toString()).isEqualTo("DEPOSIT");

                // When - Transfer money
                TransferRequest transferRequest = new TransferRequest(receiverPhone, 100.00);
                mockMvc.perform(post("/api/wallet/send")
                                .header("Authorization", "Bearer " + senderToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(transferRequest))
                                .with(csrf()))
                                .andExpect(status().isOk());

                // Then - Verify sender has transfer sent notification
                MvcResult senderTransferNotificationsResult = mockMvc.perform(get("/api/notifications")
                                .header("Authorization", "Bearer " + senderToken))
                                .andExpect(status().isOk())
                                .andReturn();

                NotificationResponse[] senderTransferNotifications = objectMapper.readValue(
                                senderTransferNotificationsResult.getResponse().getContentAsString(),
                                NotificationResponse[].class);
                assertThat(senderTransferNotifications).hasSize(2); // Deposit + Transfer sent

                // Find the transfer sent notification
                NotificationResponse transferSentNotification = null;
                for (NotificationResponse notification : senderTransferNotifications) {
                        if ("TRANSFER_SENT".equals(notification.getType().toString())) {
                                transferSentNotification = notification;
                                break;
                        }
                }
                assertThat(transferSentNotification).isNotNull();
                assertThat(transferSentNotification.getTitle()).isEqualTo("Money Sent");
                assertThat(transferSentNotification.getMessage()).contains("$100.00");
                assertThat(transferSentNotification.isRead()).isFalse();

                // Verify receiver has transfer received notification
                MvcResult receiverNotificationsResult = mockMvc.perform(get("/api/notifications")
                                .header("Authorization", "Bearer " + receiverToken))
                                .andExpect(status().isOk())
                                .andReturn();

                NotificationResponse[] receiverNotifications = objectMapper.readValue(
                                receiverNotificationsResult.getResponse().getContentAsString(),
                                NotificationResponse[].class);
                assertThat(receiverNotifications).hasSize(1);
                assertThat(receiverNotifications[0].getType().toString()).isEqualTo("TRANSFER_RECEIVED");
                assertThat(receiverNotifications[0].getTitle()).isEqualTo("Money Received");
                assertThat(receiverNotifications[0].getMessage()).contains("$100.00");
                assertThat(receiverNotifications[0].isRead()).isFalse();
        }

        @Test
        void shouldCreateDepositNotificationWhenFundsAreAdded() throws Exception {
                // When - Add funds
                mockMvc.perform(post("/api/wallet/add-funds")
                                .header("Authorization", "Bearer " + senderToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"amount\": 150.00}")
                                .with(csrf()))
                                .andExpect(status().isOk());

                // Then - Verify deposit notification was created
                MvcResult notificationsResult = mockMvc.perform(get("/api/notifications")
                                .header("Authorization", "Bearer " + senderToken))
                                .andExpect(status().isOk())
                                .andReturn();

                NotificationResponse[] notifications = objectMapper.readValue(
                                notificationsResult.getResponse().getContentAsString(),
                                NotificationResponse[].class);
                assertThat(notifications).hasSize(1);
                assertThat(notifications[0].getType().toString()).isEqualTo("DEPOSIT");
                assertThat(notifications[0].getTitle()).isEqualTo("Funds Added");
                assertThat(notifications[0].getMessage()).contains("$150.00");
                assertThat(notifications[0].isRead()).isFalse();
        }

        @Test
        void shouldGetUnreadNotificationsOnly() throws Exception {
                // Given - Add funds to create a notification
                mockMvc.perform(post("/api/wallet/add-funds")
                                .header("Authorization", "Bearer " + senderToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"amount\": 100.00}")
                                .with(csrf()))
                                .andExpect(status().isOk());

                // Get the notification ID
                MvcResult allNotificationsResult = mockMvc.perform(get("/api/notifications")
                                .header("Authorization", "Bearer " + senderToken))
                                .andExpect(status().isOk())
                                .andReturn();

                NotificationResponse[] allNotifications = objectMapper.readValue(
                                allNotificationsResult.getResponse().getContentAsString(),
                                NotificationResponse[].class);
                String notificationId = allNotifications[0].getId();

                // Mark notification as read
                mockMvc.perform(put("/api/notifications/{notificationId}/read", notificationId)
                                .header("Authorization", "Bearer " + senderToken)
                                .with(csrf()))
                                .andExpect(status().isOk());

                // When - Get unread notifications
                MvcResult unreadResult = mockMvc.perform(get("/api/notifications/unread")
                                .header("Authorization", "Bearer " + senderToken))
                                .andExpect(status().isOk())
                                .andReturn();

                // Then - Should be empty since we marked the only notification as read
                NotificationResponse[] unreadNotifications = objectMapper.readValue(
                                unreadResult.getResponse().getContentAsString(),
                                NotificationResponse[].class);
                assertThat(unreadNotifications).isEmpty();
        }

        @Test
        void shouldGetNotificationSummary() throws Exception {
                // Given - Add funds twice to create notifications
                mockMvc.perform(post("/api/wallet/add-funds")
                                .header("Authorization", "Bearer " + senderToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"amount\": 100.00}")
                                .with(csrf()))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/api/wallet/add-funds")
                                .header("Authorization", "Bearer " + senderToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"amount\": 50.00}")
                                .with(csrf()))
                                .andExpect(status().isOk());

                // When - Get notification summary
                MvcResult summaryResult = mockMvc.perform(get("/api/notifications/summary")
                                .header("Authorization", "Bearer " + senderToken))
                                .andExpect(status().isOk())
                                .andReturn();

                // Then - Should show 2 total, 2 unread
                NotificationSummaryResponse summary = objectMapper.readValue(
                                summaryResult.getResponse().getContentAsString(),
                                NotificationSummaryResponse.class);
                assertThat(summary.getTotalCount()).isEqualTo(2);
                assertThat(summary.getUnreadCount()).isEqualTo(2);
        }

        @Test
        void shouldMarkNotificationAsRead() throws Exception {
                // Given - Add funds to create a notification
                mockMvc.perform(post("/api/wallet/add-funds")
                                .header("Authorization", "Bearer " + senderToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"amount\": 100.00}")
                                .with(csrf()))
                                .andExpect(status().isOk());

                // Get the notification ID
                MvcResult notificationsResult = mockMvc.perform(get("/api/notifications")
                                .header("Authorization", "Bearer " + senderToken))
                                .andExpect(status().isOk())
                                .andReturn();

                NotificationResponse[] notifications = objectMapper.readValue(
                                notificationsResult.getResponse().getContentAsString(),
                                NotificationResponse[].class);
                String notificationId = notifications[0].getId();

                // When - Mark notification as read
                mockMvc.perform(put("/api/notifications/{notificationId}/read", notificationId)
                                .header("Authorization", "Bearer " + senderToken)
                                .with(csrf()))
                                .andExpect(status().isOk());

                // Then - Verify notification is marked as read
                MvcResult updatedNotificationsResult = mockMvc.perform(get("/api/notifications")
                                .header("Authorization", "Bearer " + senderToken))
                                .andExpect(status().isOk())
                                .andReturn();

                NotificationResponse[] updatedNotifications = objectMapper.readValue(
                                updatedNotificationsResult.getResponse().getContentAsString(),
                                NotificationResponse[].class);
                assertThat(updatedNotifications[0].isRead()).isTrue();
        }

        @Test
        void shouldReturnBadRequestForInvalidNotificationId() throws Exception {
                // When & Then
                mockMvc.perform(put("/api/notifications/{notificationId}/read", "invalid-uuid")
                                .header("Authorization", "Bearer " + senderToken)
                                .with(csrf()))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnUnauthorizedWithoutToken() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/notifications"))
                                .andExpect(status().isUnauthorized());
        }
}