package com.mobilebanking.transaction.application;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.shared.domain.exception.InsufficientFundsException;
import com.mobilebanking.shared.domain.exception.UserNotFoundException;
import com.mobilebanking.transaction.domain.MoneyTransferService;
import com.mobilebanking.transaction.domain.Transaction;
import com.mobilebanking.transaction.infrastructure.TransactionRepository;
import com.mobilebanking.notification.domain.NotificationService;
import com.mobilebanking.observability.ObservabilityService;
import com.mobilebanking.user.domain.HashedPin;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.domain.UserName;
import com.mobilebanking.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTransferTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private MoneyTransferService moneyTransferService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ObservabilityService observabilityService;

    @Mock
    private Authentication authentication;

    private WalletService walletService;

    private User sender;
    private User recipient;
    private UserId senderId;
    private UserId recipientId;
    private PhoneNumber recipientPhone;

    @BeforeEach
    void setup() {
        // Create sender with initial balance
        senderId = UserId.generate();
        UserName senderName = UserName.of("John Sender");
        PhoneNumber senderPhone = PhoneNumber.of("1234567890");
        HashedPin senderPin = HashedPin.fromRawPin("1234");
        sender = User.reconstitute(senderId, senderName, senderPhone, senderPin, Money.of(1000.00));

        // Create recipient with zero balance
        recipientId = UserId.generate();
        UserName recipientName = UserName.of("Jane Recipient");
        recipientPhone = PhoneNumber.of("9876543210");
        HashedPin recipientPin = HashedPin.fromRawPin("4321");
        recipient = User.reconstitute(recipientId, recipientName, recipientPhone, recipientPin, Money.zero());

        // Initialize the service with mocked dependencies
        walletService = new WalletService(userRepository, transactionRepository, moneyTransferService,
                notificationService, observabilityService) {
            @Override
            protected Authentication getAuthentication() {
                return authentication;
            }
        };

        // Mock authentication to return sender's ID
        when(authentication.getName()).thenReturn(senderId.asString());
        when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    void shouldTransferMoneySuccessfully() {
        // Given
        Money transferAmount = Money.of(100.00);
        when(userRepository.findByUserIdForUpdate(senderId.asString())).thenReturn(Optional.of(sender));
        when(userRepository.findByPhoneForUpdate(recipientPhone)).thenReturn(Optional.of(recipient));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Transaction transaction = walletService.transferMoney(recipientPhone.getValue(), transferAmount);

        // Then
        assertNotNull(transaction);
        assertEquals(senderId.asString(), transaction.getSenderId().asString());
        assertEquals(recipientId.asString(), transaction.getReceiverId().asString());
        assertEquals(new BigDecimal("100.00").setScale(2), transaction.getAmount().getAmount());

        // Verify balances were updated
        assertEquals(new BigDecimal("900.00").setScale(2), sender.getBalance().getAmount());
        assertEquals(new BigDecimal("100.00").setScale(2), recipient.getBalance().getAmount());

        // Verify repository calls
        verify(userRepository).save(sender);
        verify(userRepository).save(recipient);
        verify(transactionRepository).save(any(Transaction.class));
        verify(moneyTransferService).validateTransferRequest(
                sender.getId(), recipient.getId(), transferAmount, Money.of(1000.00));
    }

    @Test
    void shouldThrowExceptionWhenRecipientNotFound() {
        // Given
        Money transferAmount = Money.of(100.00);
        when(userRepository.findByUserIdForUpdate(senderId.asString())).thenReturn(Optional.of(sender));
        when(userRepository.findByPhoneForUpdate(recipientPhone)).thenReturn(Optional.empty());

        // When/Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            walletService.transferMoney(recipientPhone.getValue(), transferAmount);
        });

        // Verify exception message
        assertTrue(exception.getMessage().contains("User with phone"));

        // Verify no changes were made
        assertEquals(new BigDecimal("1000.00").setScale(2), sender.getBalance().getAmount());
        verify(userRepository, never()).save(any(User.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(moneyTransferService, never()).validateTransferRequest(any(), any(), any(), any());
    }

    @Test
    void shouldThrowExceptionWithInsufficientFunds() {
        // Given
        Money transferAmount = Money.of(2000.00);
        when(userRepository.findByUserIdForUpdate(senderId.asString())).thenReturn(Optional.of(sender));
        when(userRepository.findByPhoneForUpdate(recipientPhone)).thenReturn(Optional.of(recipient));
        doThrow(InsufficientFundsException.forTransfer(transferAmount, sender.getBalance()))
                .when(moneyTransferService).validateTransferRequest(any(), any(), any(), any());

        // When/Then
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> {
            walletService.transferMoney(recipientPhone.getValue(), transferAmount);
        });

        // Verify exception message
        assertTrue(exception.getMessage().contains("Cannot transfer"));

        // Verify no changes were made
        assertEquals(new BigDecimal("1000.00").setScale(2), sender.getBalance().getAmount());
        assertEquals(new BigDecimal("0.00").setScale(2), recipient.getBalance().getAmount());
        verify(userRepository, never()).save(any(User.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenTransferringToSelf() {
        // Given
        Money transferAmount = Money.of(100.00);
        PhoneNumber senderPhone = sender.getPhone();
        when(userRepository.findByUserIdForUpdate(senderId.asString())).thenReturn(Optional.of(sender));
        when(userRepository.findByPhoneForUpdate(senderPhone)).thenReturn(Optional.of(sender));
        doThrow(new IllegalArgumentException("Cannot transfer money to yourself"))
                .when(moneyTransferService).validateTransferRequest(any(), any(), any(), any());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            walletService.transferMoney(senderPhone.getValue(), transferAmount);
        });

        // Verify exception message
        assertEquals("Cannot transfer money to yourself", exception.getMessage());

        // Verify no changes were made
        assertEquals(new BigDecimal("1000.00").setScale(2), sender.getBalance().getAmount());
        verify(userRepository, never()).save(any(User.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWithZeroAmount() {
        // Given
        Money transferAmount = Money.zero();
        when(userRepository.findByUserIdForUpdate(senderId.asString())).thenReturn(Optional.of(sender));
        when(userRepository.findByPhoneForUpdate(recipientPhone)).thenReturn(Optional.of(recipient));
        doThrow(new IllegalArgumentException("Transfer amount must be greater than zero"))
                .when(moneyTransferService).validateTransferRequest(any(), any(), any(), any());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            walletService.transferMoney(recipientPhone.getValue(), transferAmount);
        });

        // Verify exception message
        assertEquals("Transfer amount must be greater than zero", exception.getMessage());

        // Verify no changes were made
        assertEquals(new BigDecimal("1000.00").setScale(2), sender.getBalance().getAmount());
        assertEquals(new BigDecimal("0.00").setScale(2), recipient.getBalance().getAmount());
        verify(userRepository, never()).save(any(User.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}