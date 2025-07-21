package com.mobilebanking.auth.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private static final String REGISTER_ENDPOINT = "/api/auth/register";

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber", containsString("valid format")));
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
}