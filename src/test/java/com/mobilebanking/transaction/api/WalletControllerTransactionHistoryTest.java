package com.mobilebanking.transaction.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilebanking.auth.domain.JwtTokenService;
import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.transaction.api.dto.TransactionHistoryResponse;
import com.mobilebanking.transaction.domain.Transaction;
import com.mobilebanking.transaction.infrastructure.TransactionRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WalletControllerTransactionHistoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    private User sender;
    private User receiver;
    private String senderJwtToken;

    @BeforeEach
     void setup() {
        // Create test users
        sender = User.create(
                UserName.of("Sender User"),
                PhoneNumber.of("+12345678901"),
                "1234");

        receiver = User.create(
                UserName.of("Receiver User"),
                PhoneNumber.of("+19876543210"),
                "4321");

        // Save users to the database
        sender = userRepository.save(sender);
        receiver = userRepository.save(receiver);

        // Credit balance to the sender
        sender.creditBalance(Money.of(1000.0));
        userRepository.save(sender);

        // Generate JWT token for the sender
        senderJwtToken = jwtTokenService.generateToken(sender.getId());

        // Create some test transactions
        // 1. Deposit transaction
        Transaction deposit = Transaction.createDeposit(sender.getId(), Money.of(500.0));
        transactionRepository.save(deposit);

        // 2. Transfer transaction
        Transaction transfer = Transaction.createTransfer(sender.getId(), receiver.getId(), Money.of(200.0));
        transactionRepository.save(transfer);
    }

    @Test
     void getTransactionHistory_withValidToken_returnsTransactions() throws Exception {
        // When: A request is made with a valid JWT token
        MvcResult result = mockMvc.perform(get("/api/wallet/transactions")
                .header("Authorization", "Bearer " + senderJwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then: The response contains the correct transactions
        String responseJson = result.getResponse().getContentAsString();
        TransactionHistoryResponse response = objectMapper.readValue(responseJson, TransactionHistoryResponse.class);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertNotNull(response.getData());
        assertEquals(2, response.getData().getTotalCount());
        assertEquals(2, response.getData().getTransactions().size());

        // Verify transaction types
        boolean hasDeposit = false;
        boolean hasTransfer = false;

        for (TransactionHistoryResponse.TransactionDto tx : response.getData().getTransactions()) {
            if ("deposit".equals(tx.getType())) {
                hasDeposit = true;
                assertEquals("deposit", tx.getDirection());
                assertEquals(500.0, tx.getAmount(), 0.001);
                assertNull(tx.getCounterpartyId());
            } else if ("transfer".equals(tx.getType())) {
                hasTransfer = true;
                assertEquals("sent", tx.getDirection());
                assertEquals(200.0, tx.getAmount(), 0.001);
                assertEquals(receiver.getId().asString(), tx.getCounterpartyId());
            }
        }

        assertTrue(hasDeposit, "Response should include a deposit transaction");
        assertTrue(hasTransfer, "Response should include a transfer transaction");
    }

    @Test
     void getTransactionHistoryPaginated_withValidToken_returnsPaginatedTransactions() throws Exception {
        // Create additional transactions to test pagination
        for (int i = 0; i < 5; i++) {
            Transaction deposit = Transaction.createDeposit(sender.getId(), Money.of(100.0 * (i + 1)));
            transactionRepository.save(deposit);
        }

        // When: A request is made with a valid JWT token and pagination parameters
        MvcResult result = mockMvc.perform(get("/api/wallet/transactions/paged")
                .param("page", "0")
                .param("size", "3")
                .header("Authorization", "Bearer " + senderJwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then: The response contains the correct paginated transactions
        String responseJson = result.getResponse().getContentAsString();
        TransactionHistoryResponse response = objectMapper.readValue(responseJson, TransactionHistoryResponse.class);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertNotNull(response.getData());
        assertEquals(7, response.getData().getTotalCount()); // 2 original + 5 new transactions
        assertEquals(3, response.getData().getTransactions().size()); // Page size is 3

        // Verify pagination metadata
        assertNotNull(response.getData().getPageNumber());
        assertEquals(0, response.getData().getPageNumber());
        assertEquals(3, response.getData().getPageSize());
        assertEquals(3, response.getData().getTotalPages());
        assertTrue(response.getData().getIsFirstPage());
        assertFalse(response.getData().getIsLastPage());
        assertTrue(response.getData().getHasNextPage());
        assertFalse(response.getData().getHasPreviousPage());

        // Get the second page
        result = mockMvc.perform(get("/api/wallet/transactions/paged")
                .param("page", "1")
                .param("size", "3")
                .header("Authorization", "Bearer " + senderJwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        responseJson = result.getResponse().getContentAsString();
        response = objectMapper.readValue(responseJson, TransactionHistoryResponse.class);

        assertEquals(1, response.getData().getPageNumber());
        assertEquals(3, response.getData().getTransactions().size());
        assertFalse(response.getData().getIsFirstPage());
        assertFalse(response.getData().getIsLastPage());
        assertTrue(response.getData().getHasNextPage());
        assertTrue(response.getData().getHasPreviousPage());
    }

    @Test
     void getTransactionHistory_withoutToken_returnsUnauthorized() throws Exception {
        // When: A request is made without a JWT token
        mockMvc.perform(get("/api/wallet/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Spring Security returns 403 Forbidden
    }

    @Test
     void getTransactionHistoryPaginated_withoutToken_returnsUnauthorized() throws Exception {
        // When: A request is made without a JWT token
        mockMvc.perform(get("/api/wallet/transactions/paged")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Spring Security returns 403 Forbidden
    }
}