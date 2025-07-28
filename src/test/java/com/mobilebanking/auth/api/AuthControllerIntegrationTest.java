package com.mobilebanking.auth.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilebanking.auth.api.dto.LoginRequest;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.user.api.dto.UserRegistrationRequest;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.domain.UserName;
import com.mobilebanking.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the AuthController registration endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        private static final String REGISTER_ENDPOINT = "/api/auth/register";
        private static final String LOGIN_ENDPOINT = "/api/auth/login";

        @BeforeEach
        void setUp() {
                // Clear any existing users
                userRepository.deleteAll();
        }

        @Test
        void shouldRegisterUserSuccessfully() throws Exception {
                // Given
                UserRegistrationRequest request = new UserRegistrationRequest(
                                "John Doe",
                                "+1234567890",
                                "123456");

                // When & Then
                mockMvc.perform(post(REGISTER_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.userId", notNullValue()))
                                .andExpect(jsonPath("$.name", is("John Doe")))
                                .andExpect(jsonPath("$.phoneNumber", is("+1234567890")))
                                .andExpect(jsonPath("$.message", is("User registered successfully")));
        }

        @Test
        void shouldRejectDuplicatePhoneNumber() throws Exception {
                // Given
                String phoneNumber = "+1234567890";
                User existingUser = User.create(
                                UserName.of("Existing User"),
                                PhoneNumber.of(phoneNumber),
                                "123456");
                userRepository.save(existingUser);

                UserRegistrationRequest request = new UserRegistrationRequest(
                                "John Doe",
                                phoneNumber,
                                "123456");

                // When & Then
                mockMvc.perform(post(REGISTER_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message", is("Phone number already registered")));
        }

    @Test
    void shouldRejectInvalidName() throws Exception {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest(
                "J", // Too short
                "+1234567890",
                "123456");

        // When & Then
        mockMvc.perform(post(REGISTER_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", containsString("between 2 and 50")));
    }

        @Test
        void shouldRejectInvalidPhoneNumber() throws Exception {
                // Given
                UserRegistrationRequest request = new UserRegistrationRequest(
                                "John Doe",
                                "invalid", // Invalid format
                                "123456");

                // When & Then
                mockMvc.perform(post(REGISTER_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest()) ;
        }

        @Test
        void shouldRejectInvalidPin() throws Exception {
                // Given
                UserRegistrationRequest request = new UserRegistrationRequest(
                                "John Doe",
                                "+1234567890",
                                "abc" // Non-numeric
                );

                // When & Then
                mockMvc.perform(post(REGISTER_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.pin", containsString("only digits")));
        }

        @Test
        void shouldRejectEmptyRequest() throws Exception {
                // Given
                UserRegistrationRequest request = new UserRegistrationRequest(
                                "",
                                "",
                                "");

                // When & Then
                mockMvc.perform(post(REGISTER_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.name", notNullValue()))
                                .andExpect(jsonPath("$.phoneNumber", notNullValue()))
                                .andExpect(jsonPath("$.pin", notNullValue()));
        }

        @Test
        void shouldLoginSuccessfully() throws Exception {
                // Given
                String phoneNumber = "+1234567890";
                String pin = "123456";

                // Create a user first
                User user = User.create(
                                UserName.of("Test User"),
                                PhoneNumber.of(phoneNumber),
                                pin);
                userRepository.save(user);

                LoginRequest request = new LoginRequest(phoneNumber, pin);

                // When & Then
                mockMvc.perform(post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.token", notNullValue()))
                                .andExpect(jsonPath("$.message", is("Authentication successful")));
        }

        @Test
        void shouldRejectLoginWithInvalidCredentials() throws Exception {
                // Given
                String phoneNumber = "+1234567890";
                String correctPin = "123456";
                String wrongPin = "654321";

                // Create a user first
                User user = User.create(
                                UserName.of("Test User"),
                                PhoneNumber.of(phoneNumber),
                                correctPin);
                userRepository.save(user);

                LoginRequest request = new LoginRequest(phoneNumber, wrongPin);

                // When & Then
                mockMvc.perform(post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.token").doesNotExist());
        }

        @Test
        void shouldRejectLoginWithNonExistentUser() throws Exception {
                // Given
                String nonExistentPhoneNumber = "+9999999999";
                LoginRequest request = new LoginRequest(nonExistentPhoneNumber, "123456");

                // When & Then
                mockMvc.perform(post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.token").doesNotExist())
                                .andExpect(jsonPath("$.message", is("Invalid phone number or PIN")));
        }

        @Test
        void shouldRejectLoginWithEmptyCredentials() throws Exception {
                // Given
                LoginRequest request = new LoginRequest("", "");

                // When & Then
                mockMvc.perform(post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.phoneNumber", notNullValue()))
                                .andExpect(jsonPath("$.pin", notNullValue()));
        }
}