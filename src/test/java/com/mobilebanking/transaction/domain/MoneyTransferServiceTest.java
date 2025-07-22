package com.mobilebanking.transaction.domain;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.shared.domain.exception.InsufficientFundsException;
import com.mobilebanking.shared.domain.exception.SelfTransferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MoneyTransferService Domain Tests")
class MoneyTransferServiceTest {

    private MoneyTransferService moneyTransferService;
    private final UserId senderId = UserId.generate();
    private final UserId receiverId = UserId.generate();
    private final Money transferAmount = Money.of(100.00);
    private final Money sufficientBalance = Money.of(200.00);
    private final Money insufficientBalance = Money.of(50.00);

    @BeforeEach
    void setUp() {
        moneyTransferService = new MoneyTransferService();
    }

    @Test
    @DisplayName("Should create transfer successfully with sufficient balance")
    void shouldCreateTransferSuccessfullyWithSufficientBalance() {
        // When
        Transaction transaction = moneyTransferService.createTransfer(
                senderId, receiverId, transferAmount, sufficientBalance);

        // Then
        assertNotNull(transaction);
        assertEquals(senderId, transaction.getSenderId());
        assertEquals(receiverId, transaction.getReceiverId());
        assertEquals(transferAmount, transaction.getAmount());
        assertEquals(TransactionType.TRANSFER, transaction.getType());
    }

    @Test
    @DisplayName("Should throw exception when creating transfer with insufficient balance")
    void shouldThrowExceptionWhenCreatingTransferWithInsufficientBalance() {
        // When & Then
        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> moneyTransferService.createTransfer(senderId, receiverId, transferAmount, insufficientBalance));
        assertTrue(exception.getMessage().contains("Cannot transfer"));
        assertTrue(exception.getMessage().contains(transferAmount.toString()));
        assertTrue(exception.getMessage().contains(insufficientBalance.toString()));
    }

    @Test
    @DisplayName("Should validate transfer request successfully")
    void shouldValidateTransferRequestSuccessfully() {
        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> moneyTransferService.validateTransferRequest(senderId, receiverId, transferAmount,
                sufficientBalance));
    }

    @Test
    @DisplayName("Should throw exception when validating transfer with null sender")
    void shouldThrowExceptionWhenValidatingTransferWithNullSender() {
        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> moneyTransferService.validateTransferRequest(null, receiverId, transferAmount,
                        sufficientBalance));
        assertEquals("Sender ID cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when validating transfer with null receiver")
    void shouldThrowExceptionWhenValidatingTransferWithNullReceiver() {
        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> moneyTransferService.validateTransferRequest(senderId, null, transferAmount, sufficientBalance));
        assertEquals("Receiver ID cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when validating transfer with null amount")
    void shouldThrowExceptionWhenValidatingTransferWithNullAmount() {
        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> moneyTransferService.validateTransferRequest(senderId, receiverId, null, sufficientBalance));
        assertEquals("Transfer amount cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when validating transfer with null balance")
    void shouldThrowExceptionWhenValidatingTransferWithNullBalance() {
        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> moneyTransferService.validateTransferRequest(senderId, receiverId, transferAmount, null));
        assertEquals("Sender balance cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw SelfTransferException when validating transfer to same user")
    void shouldThrowExceptionWhenValidatingTransferToSameUser() {
        // When & Then
        SelfTransferException exception = assertThrows(
                SelfTransferException.class,
                () -> moneyTransferService.validateTransferRequest(senderId, senderId, transferAmount,
                        sufficientBalance));
        assertEquals("Cannot transfer money to yourself", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when validating transfer with zero amount")
    void shouldThrowExceptionWhenValidatingTransferWithZeroAmount() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> moneyTransferService.validateTransferRequest(senderId, receiverId, Money.zero(),
                        sufficientBalance));
        assertEquals("Transfer amount must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should correctly check sufficient balance")
    void shouldCorrectlyCheckSufficientBalance() {
        // When & Then
        assertTrue(moneyTransferService.hasSufficientBalance(sufficientBalance, transferAmount));
        assertTrue(moneyTransferService.hasSufficientBalance(transferAmount, transferAmount)); // Equal amounts
        assertFalse(moneyTransferService.hasSufficientBalance(insufficientBalance, transferAmount));
    }

    @Test
    @DisplayName("Should throw exception when checking balance with null parameters")
    void shouldThrowExceptionWhenCheckingBalanceWithNullParameters() {
        // When & Then
        assertThrows(NullPointerException.class,
                () -> moneyTransferService.hasSufficientBalance(null, transferAmount));
        assertThrows(NullPointerException.class,
                () -> moneyTransferService.hasSufficientBalance(sufficientBalance, null));
    }

    @Test
    @DisplayName("Should calculate balance after debit correctly")
    void shouldCalculateBalanceAfterDebitCorrectly() {
        // Given
        Money expectedBalance = Money.of(100.00); // 200 - 100

        // When
        Money newBalance = moneyTransferService.calculateBalanceAfterDebit(sufficientBalance, transferAmount);

        // Then
        assertEquals(expectedBalance, newBalance);
    }

    @Test
    @DisplayName("Should throw exception when debiting more than available balance")
    void shouldThrowExceptionWhenDebitingMoreThanAvailableBalance() {
        // When & Then
        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> moneyTransferService.calculateBalanceAfterDebit(insufficientBalance, transferAmount));
        assertTrue(exception.getMessage().contains("Insufficient funds"));
    }

    @Test
    @DisplayName("Should throw exception when calculating debit with null parameters")
    void shouldThrowExceptionWhenCalculatingDebitWithNullParameters() {
        // When & Then
        assertThrows(NullPointerException.class,
                () -> moneyTransferService.calculateBalanceAfterDebit(null, transferAmount));
        assertThrows(NullPointerException.class,
                () -> moneyTransferService.calculateBalanceAfterDebit(sufficientBalance, null));
    }

    @Test
    @DisplayName("Should calculate balance after credit correctly")
    void shouldCalculateBalanceAfterCreditCorrectly() {
        // Given
        Money initialBalance = Money.of(50.00);
        Money creditAmount = Money.of(25.00);
        Money expectedBalance = Money.of(75.00);

        // When
        Money newBalance = moneyTransferService.calculateBalanceAfterCredit(initialBalance, creditAmount);

        // Then
        assertEquals(expectedBalance, newBalance);
    }

    @Test
    @DisplayName("Should throw exception when calculating credit with null parameters")
    void shouldThrowExceptionWhenCalculatingCreditWithNullParameters() {
        // When & Then
        assertThrows(NullPointerException.class,
                () -> moneyTransferService.calculateBalanceAfterCredit(null, transferAmount));
        assertThrows(NullPointerException.class,
                () -> moneyTransferService.calculateBalanceAfterCredit(sufficientBalance, null));
    }

    @Test
    @DisplayName("Should validate transfer amount correctly")
    void shouldValidateTransferAmountCorrectly() {
        // When & Then
        assertTrue(moneyTransferService.isValidTransferAmount(transferAmount));
        assertFalse(moneyTransferService.isValidTransferAmount(Money.zero()));
    }

    @Test
    @DisplayName("Should throw exception when validating transfer amount with null")
    void shouldThrowExceptionWhenValidatingTransferAmountWithNull() {
        // When & Then
        assertThrows(NullPointerException.class,
                () -> moneyTransferService.isValidTransferAmount(null));
    }
}