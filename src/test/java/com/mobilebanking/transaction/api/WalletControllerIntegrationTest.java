package com.mobilebanking.transaction.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilebanking.auth.domain.JwtTokenService;
import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.transaction.api.dto.BalanceResponse;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.domain.UserName;
import com.mobilebanking.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    // No need for WalletService as we're testing the controller directly

    private String jwtToken;

    @BeforeEach
    void setup() {
        // Create a test user
        User testUser = User.create(
                UserName.of("Test User"),
                PhoneNumber.of("1234567890"),
                "1234");

        // Save the user to the database
        testUser = userRepository.save(testUser);

        // Credit some balance to the user
        testUser.creditBalance(Money.of(1000.0));
        userRepository.save(testUser);

        // Generate JWT token for the test user
        jwtToken = jwtTokenService.generateToken(testUser.getId());
    }

    @Test
    void getBalance_withValidToken_returnsBalance() throws Exception {
        // When: A request is made with a valid JWT token
        MvcResult result = mockMvc.perform(get("/api/wallet/balance")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then: The response contains the correct balance
        String responseJson = result.getResponse().getContentAsString();
        BalanceResponse response = objectMapper.readValue(responseJson, BalanceResponse.class);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertNotNull(response.getData());
        assertEquals(1000.0, response.getData().getAmount(), 0.001);
    }

    @Test
    void getBalance_withoutToken_returnsUnauthorized() throws Exception {
        // When: A request is made without a JWT token
        mockMvc.perform(get("/api/wallet/balance")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBalance_withInvalidToken_returnsUnauthorized() throws Exception {
        // When: A request is made with an invalid JWT token
        mockMvc.perform(get("/api/wallet/balance")
                .header("Authorization", "Bearer invalidtoken")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}