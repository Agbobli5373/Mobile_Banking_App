package com.mobilebanking.transaction.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilebanking.auth.domain.JwtTokenService;
import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.transaction.api.dto.DepositRequest;
import com.mobilebanking.transaction.api.dto.DepositResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class WalletControllerDepositTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    private User testUser;
    private String jwtToken;

    @BeforeEach
    public void setup() {
        // Create a test user
        testUser = User.create(
                UserName.of("Test User"),
                PhoneNumber.of("1234567890"),
                "1234");

        // Save the user to the database
        testUser = userRepository.save(testUser);

        // Credit some balance to the user
        testUser.creditBalance(Money.of(100.0));
        userRepository.save(testUser);

        // Generate JWT token for the test user
        jwtToken = jwtTokenService.generateToken(testUser.getId());
    }

    @Test
    public void addFunds_withValidRequest_returnsSuccess() throws Exception {
        // Given: A valid deposit request
        DepositRequest request = new DepositRequest(50.0);

        // When: A deposit request is made with a valid JWT token
        MvcResult result = mockMvc.perform(post("/api/wallet/deposit")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Then: The response contains success status and transaction details
        String responseJson = result.getResponse().getContentAsString();
        DepositResponse response = objectMapper.readValue(responseJson, DepositResponse.class);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("Funds added successfully", response.getMessage());
        assertNotNull(response.getData());
        assertNotNull(response.getData().getTransactionId());
        assertEquals(50.0, response.getData().getAmount(), 0.001);
        assertEquals(150.0, response.getData().getNewBalance(), 0.001); // 100 + 50
        assertEquals("USD", response.getData().getCurrency());

        // Verify the user's balance was updated in the database
        User updatedUser = userRepository.findByUserId(testUser.getId()).orElseThrow();
        assertEquals(Money.of(150.0), updatedUser.getBalance());
    }

    @Test
    public void addFunds_withZeroAmount_returnsBadRequest() throws Exception {
        // Given: A deposit request with zero amount
        DepositRequest request = new DepositRequest(0.0);

        // When: A deposit request is made
        mockMvc.perform(post("/api/wallet/deposit")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify the user's balance was not changed
        User unchangedUser = userRepository.findByUserId(testUser.getId()).orElseThrow();
        assertEquals(Money.of(100.0), unchangedUser.getBalance());
    }

    @Test
    public void addFunds_withNegativeAmount_returnsBadRequest() throws Exception {
        // Given: A deposit request with negative amount
        DepositRequest request = new DepositRequest(-50.0);

        // When: A deposit request is made
        mockMvc.perform(post("/api/wallet/deposit")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify the user's balance was not changed
        User unchangedUser = userRepository.findByUserId(testUser.getId()).orElseThrow();
        assertEquals(Money.of(100.0), unchangedUser.getBalance());
    }

    @Test
    public void addFunds_withoutToken_returnsUnauthorized() throws Exception {
        // Given: A valid deposit request
        DepositRequest request = new DepositRequest(50.0);

        // When: A deposit request is made without JWT token
        mockMvc.perform(post("/api/wallet/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void addFunds_withInvalidToken_returnsUnauthorized() throws Exception {
        // Given: A valid deposit request
        DepositRequest request = new DepositRequest(50.0);

        // When: A deposit request is made with invalid JWT token
        mockMvc.perform(post("/api/wallet/deposit")
                .header("Authorization", "Bearer invalidtoken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void addFunds_withLargeAmount_returnsSuccess() throws Exception {
        // Given: A deposit request with large amount
        DepositRequest request = new DepositRequest(10000.0);

        // When: A deposit request is made
        MvcResult result = mockMvc.perform(post("/api/wallet/deposit")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Then: The response contains success status
        String responseJson = result.getResponse().getContentAsString();
        DepositResponse response = objectMapper.readValue(responseJson, DepositResponse.class);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals(10000.0, response.getData().getAmount(), 0.001);
        assertEquals(10100.0, response.getData().getNewBalance(), 0.001); // 100 + 10000

        // Verify the user's balance was updated in the database
        User updatedUser = userRepository.findByUserId(testUser.getId()).orElseThrow();
        assertEquals(Money.of(10100.0), updatedUser.getBalance());
    }

    @Test
    public void addFunds_withDecimalAmount_returnsSuccess() throws Exception {
        // Given: A deposit request with decimal amount
        DepositRequest request = new DepositRequest(25.75);

        // When: A deposit request is made
        MvcResult result = mockMvc.perform(post("/api/wallet/deposit")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Then: The response contains success status with proper decimal handling
        String responseJson = result.getResponse().getContentAsString();
        DepositResponse response = objectMapper.readValue(responseJson, DepositResponse.class);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals(25.75, response.getData().getAmount(), 0.001);
        assertEquals(125.75, response.getData().getNewBalance(), 0.001); // 100 + 25.75

        // Verify the user's balance was updated in the database
        User updatedUser = userRepository.findByUserId(testUser.getId()).orElseThrow();
        assertEquals(Money.of(125.75), updatedUser.getBalance());
    }
}