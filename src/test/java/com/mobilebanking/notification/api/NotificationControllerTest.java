package com.mobilebanking.notification.api;

import com.mobilebanking.auth.domain.JwtTokenService;
import com.mobilebanking.notification.domain.Notification;
import com.mobilebanking.notification.domain.NotificationId;
import com.mobilebanking.notification.domain.NotificationService;
import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.UserId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for NotificationController.
 */
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private JwtTokenService jwtTokenService ;

        @MockBean
        private NotificationService notificationService;

        private final UserId testUserId = UserId.fromString("123e4567-e89b-12d3-a456-426614174000");

        @Test
        @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
        void shouldGetAllNotifications() throws Exception {
                // Given
                Money amount = Money.of(100.00);
                Notification notification1 = Notification.createDepositNotification(testUserId, amount);
                Notification notification2 = Notification.createDepositNotification(testUserId, Money.of(50.00));
                List<Notification> notifications = Arrays.asList(notification1, notification2);

                given(notificationService.getNotificationsForUser(testUserId)).willReturn(notifications);

                // When & Then
                mockMvc.perform(get("/api/notifications"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].id").value(notification1.getId().asString()))
                                .andExpect(jsonPath("$[0].title").value("Funds Added"))
                                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                                .andExpect(jsonPath("$[0].read").value(false))
                                .andExpect(jsonPath("$[1].id").value(notification2.getId().asString()));

                verify(notificationService).getNotificationsForUser(testUserId);
        }

        @Test
        @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
        void shouldGetEmptyListWhenNoNotifications() throws Exception {
                // Given
                given(notificationService.getNotificationsForUser(testUserId)).willReturn(Collections.emptyList());

                // When & Then
                mockMvc.perform(get("/api/notifications"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(0));

                verify(notificationService).getNotificationsForUser(testUserId);
        }

        @Test
        @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
        void shouldGetUnreadNotifications() throws Exception {
                // Given
                Money amount = Money.of(75.00);
                Notification unreadNotification = Notification.createDepositNotification(testUserId, amount);
                List<Notification> unreadNotifications = Collections.singletonList(unreadNotification);

                given(notificationService.getUnreadNotificationsForUser(testUserId)).willReturn(unreadNotifications);

                // When & Then
                mockMvc.perform(get("/api/notifications/unread"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].id").value(unreadNotification.getId().asString()))
                                .andExpect(jsonPath("$[0].read").value(false));

                verify(notificationService).getUnreadNotificationsForUser(testUserId);
        }

        @Test
        @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
        void shouldGetNotificationSummary() throws Exception {
                // Given
                Money amount = Money.of(100.00);
                List<Notification> allNotifications = Arrays.asList(
                                Notification.createDepositNotification(testUserId, amount),
                                Notification.createDepositNotification(testUserId, amount),
                                Notification.createDepositNotification(testUserId, amount));

                given(notificationService.getNotificationsForUser(testUserId)).willReturn(allNotifications);
                given(notificationService.getUnreadNotificationCount(testUserId)).willReturn(2);

                // When & Then
                mockMvc.perform(get("/api/notifications/summary"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.totalCount").value(3))
                                .andExpect(jsonPath("$.unreadCount").value(2));

                verify(notificationService).getNotificationsForUser(testUserId);
                verify(notificationService).getUnreadNotificationCount(testUserId);
        }

        @Test
        @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
        void shouldMarkNotificationAsRead() throws Exception {
                // Given
                NotificationId notificationId = NotificationId.generate();

                // When & Then
                mockMvc.perform(put("/api/notifications/{notificationId}/read", notificationId.asString())
                                .with(csrf()))
                                .andExpect(status().isOk());

                verify(notificationService).markNotificationAsRead(eq(notificationId), eq(testUserId));
        }

        @Test
        @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
        void shouldReturnBadRequestForInvalidNotificationId() throws Exception {
                // Given
                String invalidNotificationId = "invalid-uuid";

                // When & Then
                mockMvc.perform(put("/api/notifications/{notificationId}/read", invalidNotificationId)
                                .with(csrf()))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
        void shouldReturnBadRequestWhenNotificationDoesNotBelongToUser() throws Exception {
                // Given
                NotificationId notificationId = NotificationId.generate();
                doThrow(new IllegalArgumentException("Notification does not belong to the specified user"))
                                .when(notificationService)
                                .markNotificationAsRead(any(NotificationId.class), any(UserId.class));

                // When & Then
                mockMvc.perform(put("/api/notifications/{notificationId}/read", notificationId.asString())
                                .with(csrf()))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/notifications"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturnUnauthorizedForUnreadNotificationsWhenNotAuthenticated() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/notifications/unread"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturnUnauthorizedForSummaryWhenNotAuthenticated() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/notifications/summary"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturnUnauthorizedForMarkAsReadWhenNotAuthenticated() throws Exception {
                // Given
                NotificationId notificationId = NotificationId.generate();

                // When & Then
                mockMvc.perform(put("/api/notifications/{notificationId}/read", notificationId.asString())
                                .with(csrf()))
                                .andExpect(status().isUnauthorized());
        }
}