package com.mobilebanking.transaction.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilebanking.auth.domain.JwtTokenService;
import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.transaction.api.dto.TransferRequest;
import com.mobilebanking.transaction.domain.Transaction;
import com.mobilebanking.transaction.infrastructure.TransactionRepository;
import com.mobilebanking.user.domain.HashedPin;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class WalletControllerTransferTest {

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
        private User recipient;
        private String jwtToken;

        @BeforeEach
        public void setup() {
                // Create sender with initial balance
                UserId senderId = UserId.generate();
                UserName senderName = UserName.of("John Sender");
                PhoneNumber senderPhone = PhoneNumber.of("1234567890");
                HashedPin senderPin = HashedPin.fromRawPin("1234");
                sender = User.reconstitute(senderId, senderName, senderPhone, senderPin, Money.of(1000.00));
                userRepository.save(sender);

                // Create recipient with zero balance
                UserId recipientId = UserId.generate();
                UserName recipientName = UserName.of("Jane Recipient");
                PhoneNumber recipientPhone = PhoneNumber.of("9876543210");
                HashedPin recipientPin = HashedPin.fromRawPin("4321");
                recipient = User.reconstitute(recipientId, recipientName, recipientPhone, recipientPin, Money.zero());
                userRepository.save(recipient);

                // Generate JWT token for sender
                jwtToken = jwtTokenService.generateToken(sender.getId());
        }

        @Test
        public void shouldTransferMoneySuccessfully() throws Exception {
                // Given
                double transferAmount = 100.00;
                TransferRequest request = new TransferRequest(recipient.getPhone().getValue(), transferAmount);
                String requestJson = objectMapper.writeValueAsString(request);

                // When
                ResultActions result = mockMvc.perform(post("/api/wallet/send")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson));

                // Then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Transfer completed successfully"))
                                .andExpect(jsonPath("$.data.amount").value(transferAmount))
                                .andExpect(jsonPath("$.data.recipientPhone").value(recipient.getPhone().getValue()))
                                .andExpect(jsonPath("$.data.newBalance").value(900.00))
                                .andExpect(jsonPath("$.data.transactionId").isNotEmpty());

                // Verify database state
                User updatedSender = userRepository.findByUserId(sender.getId()).orElseThrow();
                User updatedRecipient = userRepository.findByUserId(recipient.getId()).orElseThrow();

                assertEquals(new BigDecimal("900.00").setScale(2), updatedSender.getBalance().getAmount());
                assertEquals(new BigDecimal("100.00").setScale(2), updatedRecipient.getBalance().getAmount());

                // Verify transaction was created
                Transaction transaction = transactionRepository.findByUserOrderByTimestampDesc(sender.getId()).get(0);
                assertNotNull(transaction);
                assertEquals(sender.getId().asString(), transaction.getSenderId().asString());
                assertEquals(recipient.getId().asString(), transaction.getReceiverId().asString());
                assertEquals(new BigDecimal("100.00").setScale(2), transaction.getAmount().getAmount());
        }

        @Test
        public void shouldFailWhenRecipientNotFound() throws Exception {
                // Given
                TransferRequest request = new TransferRequest("9999999999", 100.00);
                String requestJson = objectMapper.writeValueAsString(request);

                // When
                ResultActions result = mockMvc.perform(post("/api/wallet/send")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson));

                // Then
                result.andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value(containsString("Recipient not found")));

                // Verify sender balance unchanged
                User updatedSender = userRepository.findByUserId(sender.getId()).orElseThrow();
                assertEquals(new BigDecimal("1000.00").setScale(2), updatedSender.getBalance().getAmount());
        }

        @Test
        public void shouldFailWithInsufficientFunds() throws Exception {
                // Given
                TransferRequest request = new TransferRequest(recipient.getPhone().getValue(), 2000.00);
                String requestJson = objectMapper.writeValueAsString(request);

                // When
                ResultActions result = mockMvc.perform(post("/api/wallet/send")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson));

                // Then
                result.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value(containsString("Insufficient funds")));

                // Verify balances unchanged
                User updatedSender = userRepository.findByUserId(sender.getId()).orElseThrow();
                User updatedRecipient = userRepository.findByUserId(recipient.getId()).orElseThrow();
                assertEquals(new BigDecimal("1000.00").setScale(2), updatedSender.getBalance().getAmount());
                assertEquals(new BigDecimal("0.00").setScale(2), updatedRecipient.getBalance().getAmount());
        }

        @Test
        public void shouldFailWithNegativeAmount() throws Exception {
                // Given
                TransferRequest request = new TransferRequest(recipient.getPhone().getValue(), -100.00);
                String requestJson = objectMapper.writeValueAsString(request);

                // When
                ResultActions result = mockMvc.perform(post("/api/wallet/send")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson));

                // Then
                result.andExpect(status().isBadRequest());

                // Verify balances unchanged
                User updatedSender = userRepository.findByUserId(sender.getId()).orElseThrow();
                User updatedRecipient = userRepository.findByUserId(recipient.getId()).orElseThrow();
                assertEquals(new BigDecimal("1000.00").setScale(2), updatedSender.getBalance().getAmount());
                assertEquals(new BigDecimal("0.00").setScale(2), updatedRecipient.getBalance().getAmount());
        }

        @Test
        public void shouldFailWhenTransferringToSelf() throws Exception {
                // Given
                TransferRequest request = new TransferRequest(sender.getPhone().getValue(), 100.00);
                String requestJson = objectMapper.writeValueAsString(request);

                // When
                ResultActions result = mockMvc.perform(post("/api/wallet/send")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson));

                // Then
                result.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value(containsString("Cannot transfer")));

                // Verify balance unchanged
                User updatedSender = userRepository.findByUserId(sender.getId()).orElseThrow();
                assertEquals(new BigDecimal("1000.00").setScale(2), updatedSender.getBalance().getAmount());
        }

        @Test
        public void shouldFailWithoutAuthentication() throws Exception {
                // Given
                TransferRequest request = new TransferRequest(recipient.getPhone().getValue(), 100.00);
                String requestJson = objectMapper.writeValueAsString(request);

                // When
                ResultActions result = mockMvc.perform(post("/api/wallet/send")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson));

                // Then
                result.andExpect(status().isUnauthorized());

                // Verify balances unchanged
                User updatedSender = userRepository.findByUserId(sender.getId()).orElseThrow();
                User updatedRecipient = userRepository.findByUserId(recipient.getId()).orElseThrow();
                assertEquals(new BigDecimal("1000.00").setScale(2), updatedSender.getBalance().getAmount());
                assertEquals(new BigDecimal("0.00").setScale(2), updatedRecipient.getBalance().getAmount());
        }
}