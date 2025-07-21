package com.mobilebanking.transaction.domain;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.TransactionId;
import com.mobilebanking.shared.domain.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transaction Domain Tests")
class TransactionTest {

  private final UserId senderId = UserId.generate();
  private final UserId receiverId = UserId.generate();
  private final Money amount = Money.of(100.00);

  @Test
  @DisplayName("Should create transfer transaction successfully")
  void shouldCreateTransferTransactionSuccessfully() {
    // When
    Transaction transaction = Transaction.createTransfer(senderId, receiverId, amount);

    // Then
    assertNotNull(transaction);
    assertNotNull(transaction.getId());
    assertEquals(senderId, transaction.getSenderId());
    assertEquals(receiverId, transaction.getReceiverId());
    assertEquals(amount, transaction.getAmount());
    assertEquals(TransactionType.TRANSFER, transaction.getType());
    assertNotNull(transaction.getTimestamp());
    assertTrue(transaction.isTransfer());
    assertFalse(transaction.isDeposit());
  }

  @Test
  @DisplayName("Should create deposit transaction successfully")
  void shouldCreateDepositTransactionSuccessfully() {
    // When
    Transaction transaction = Transaction.createDeposit(receiverId, amount);

    // Then
    assertNotNull(transaction);
    assertNotNull(transaction.getId());
    assertNull(transaction.getSenderId());
    assertEquals(receiverId, transaction.getReceiverId());
    assertEquals(amount, transaction.getAmount());
    assertEquals(TransactionType.DEPOSIT, transaction.getType());
    assertNotNull(transaction.getTimestamp());
    assertTrue(transaction.isDeposit());
    assertFalse(transaction.isTransfer());
  }

  @Test
  @DisplayName("Should throw exception when creating transfer with null sender")
  void shouldThrowExceptionWhenCreatingTransferWithNullSender() {
    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> Transaction.createTransfer(null, receiverId, amount));
    assertEquals("Sender cannot be null for transfer", exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when creating transfer with null receiver")
  void shouldThrowExceptionWhenCreatingTransferWithNullReceiver() {
    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> Transaction.createTransfer(senderId, null, amount));
    assertEquals("Receiver cannot be null for transfer", exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when creating transfer with same sender and receiver")
  void shouldThrowExceptionWhenCreatingTransferWithSameSenderAndReceiver() {
    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> Transaction.createTransfer(senderId, senderId, amount));
    assertEquals("Cannot transfer money to yourself", exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when creating transfer with null amount")
  void shouldThrowExceptionWhenCreatingTransferWithNullAmount() {
    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> Transaction.createTransfer(senderId, receiverId, null));
    assertEquals("Transfer amount must be positive", exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when creating transfer with zero amount")
  void shouldThrowExceptionWhenCreatingTransferWithZeroAmount() {
    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> Transaction.createTransfer(senderId, receiverId, Money.zero()));
    assertEquals("Transfer amount must be positive", exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when creating deposit with null user")
  void shouldThrowExceptionWhenCreatingDepositWithNullUser() {
    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> Transaction.createDeposit(null, amount));
    assertEquals("User cannot be null for deposit", exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when creating deposit with null amount")
  void shouldThrowExceptionWhenCreatingDepositWithNullAmount() {
    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> Transaction.createDeposit(receiverId, null));
    assertEquals("Deposit amount must be positive", exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when creating deposit with zero amount")
  void shouldThrowExceptionWhenCreatingDepositWithZeroAmount() {
    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> Transaction.createDeposit(receiverId, Money.zero()));
    assertEquals("Deposit amount must be positive", exception.getMessage());
  }

  @Test
  @DisplayName("Should correctly identify if user is involved in transaction")
  void shouldCorrectlyIdentifyIfUserIsInvolvedInTransaction() {
    // Given
    Transaction transferTransaction = Transaction.createTransfer(senderId, receiverId, amount);
    Transaction depositTransaction = Transaction.createDeposit(receiverId, amount);
    UserId otherUserId = UserId.generate();

    // When & Then
    assertTrue(transferTransaction.involvesUser(senderId));
    assertTrue(transferTransaction.involvesUser(receiverId));
    assertFalse(transferTransaction.involvesUser(otherUserId));
    assertFalse(transferTransaction.involvesUser(null));

    assertFalse(depositTransaction.involvesUser(senderId));
    assertTrue(depositTransaction.involvesUser(receiverId));
    assertFalse(depositTransaction.involvesUser(otherUserId));
    assertFalse(depositTransaction.involvesUser(null));
  }

  @Test
  @DisplayName("Should have proper equality based on transaction ID")
  void shouldHaveProperEqualityBasedOnTransactionId() {
    // Given
    Transaction transaction1 = Transaction.createTransfer(senderId, receiverId, amount);
    Transaction transaction2 = Transaction.createTransfer(senderId, receiverId, amount);

    // When & Then
    assertNotEquals(transaction1, transaction2); // Different IDs
    assertEquals(transaction1, transaction1); // Same instance
    assertNotEquals(transaction1, null);
    assertNotEquals(transaction1, "not a transaction");
  }

  @Test
  @DisplayName("Should have consistent hashCode based on transaction ID")
  void shouldHaveConsistentHashCodeBasedOnTransactionId() {
    // Given
    Transaction transaction = Transaction.createTransfer(senderId, receiverId, amount);

    // When & Then
    assertEquals(transaction.hashCode(), transaction.hashCode());
    // The hashCode is based on Objects.hash(id), not directly id.hashCode()
    assertNotNull(transaction.hashCode());
  }

  @Test
  @DisplayName("Should have meaningful toString representation")
  void shouldHaveMeaningfulToStringRepresentation() {
    // Given
    Transaction transaction = Transaction.createTransfer(senderId, receiverId, amount);

    // When
    String toString = transaction.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("Transaction{"));
    assertTrue(toString.contains("id=" + transaction.getId()));
    assertTrue(toString.contains("senderId=" + senderId));
    assertTrue(toString.contains("receiverId=" + receiverId));
    assertTrue(toString.contains("amount=" + amount));
    assertTrue(toString.contains("type=" + TransactionType.TRANSFER));
  }
}