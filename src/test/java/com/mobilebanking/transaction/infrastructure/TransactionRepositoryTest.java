package com.mobilebanking.transaction.infrastructure;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.TransactionId;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.transaction.domain.Transaction;
import com.mobilebanking.transaction.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TransactionRepository using @DataJpaTest.
 * Tests the repository layer with an embedded database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TransactionRepository Integration Tests")
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    private UserId senderId;
    private UserId receiverId;
    private UserId thirdUserId;
    private Transaction transferTransaction;
    private Transaction depositTransaction;

    @BeforeEach
    void setUp() {
        senderId = UserId.generate();
        receiverId = UserId.generate();
        thirdUserId = UserId.generate();

        transferTransaction = Transaction.createTransfer(senderId, receiverId, Money.of(100.00));
        depositTransaction = Transaction.createDeposit(receiverId, Money.of(50.00));
    }

    @Test
    @DisplayName("Should save and find transaction by ID")
    void shouldSaveAndFindTransactionById() {
        // Given
        Transaction savedTransaction = transactionRepository.save(transferTransaction);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Transaction> foundTransaction = transactionRepository.findByTransactionId(savedTransaction.getId());

        // Then
        assertThat(foundTransaction).isPresent();
        assertThat(foundTransaction.get().getId()).isEqualTo(savedTransaction.getId());
        assertThat(foundTransaction.get().getSenderId()).isEqualTo(senderId);
        assertThat(foundTransaction.get().getReceiverId()).isEqualTo(receiverId);
        assertThat(foundTransaction.get().getAmount()).isEqualTo(Money.of(100.00));
        assertThat(foundTransaction.get().getType()).isEqualTo(TransactionType.TRANSFER);
    }

    @Test
    @DisplayName("Should find transactions by user ID ordered by timestamp desc")
    void shouldFindTransactionsByUserIdOrderedByTimestampDesc() {
        // Given
        Transaction olderTransaction = Transaction.createTransfer(thirdUserId, senderId, Money.of(25.00));

        transactionRepository.save(olderTransaction);
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            /* ignore */ }
        transactionRepository.save(transferTransaction);
        transactionRepository.save(depositTransaction);
        entityManager.flush();
        entityManager.clear(); // Ensure we reload from DB

        // When
        List<Transaction> senderTransactions = transactionRepository.findByUserOrderByTimestampDesc(senderId);
        System.out.println("Sender Transactions: " + senderTransactions);

        // Then
        assertThat(senderTransactions).hasSize(2); // transferTransaction (as sender) and olderTransaction (as receiver)
        // Most recent first
        assertThat(senderTransactions.get(0).getTimestamp().isAfter(senderTransactions.get(1).getTimestamp())).isTrue();
//        assertThat(senderTransactions.get(0).getId()).isEqualTo(transferTransaction.getId());
//        assertThat(senderTransactions.get(1).getId()).isEqualTo(olderTransaction.getId());
    }

    @Test
    @DisplayName("Should find transactions sent by user")
    void shouldFindTransactionsSentByUser() {
        // Given
        transactionRepository.save(transferTransaction);
        transactionRepository.save(depositTransaction);
        entityManager.flush();

        // When
        List<Transaction> sentTransactions = transactionRepository.findBySenderOrderByTimestampDesc(senderId);

        // Then
        assertThat(sentTransactions).hasSize(1);
        assertThat(sentTransactions.getFirst().getId()).isEqualTo(transferTransaction.getId());
        assertThat(sentTransactions.getFirst().getSenderId()).isEqualTo(senderId);
    }

    @Test
    @DisplayName("Should find transactions received by user")
    void shouldFindTransactionsReceivedByUser() {
        // Given
        transactionRepository.save(transferTransaction);
        transactionRepository.save(depositTransaction);
        entityManager.flush();

        // When
        List<Transaction> receivedTransactions = transactionRepository.findByReceiverOrderByTimestampDesc(receiverId);

        // Then
        assertThat(receivedTransactions).hasSize(2); // Both transfer and deposit
        assertThat(receivedTransactions).extracting(Transaction::getReceiverId)
                .containsOnly(receiverId);
    }

    @Test
    @DisplayName("Should find transactions by user and type")
    void shouldFindTransactionsByUserAndType() {
        // Given
        transactionRepository.save(transferTransaction);
        transactionRepository.save(depositTransaction);
        entityManager.flush();

        // When
        List<Transaction> transferTransactions = transactionRepository.findByUserAndTypeOrderByTimestampDesc(
                receiverId, TransactionType.TRANSFER);
        List<Transaction> depositTransactions = transactionRepository.findByUserAndTypeOrderByTimestampDesc(
                receiverId, TransactionType.DEPOSIT);

        // Then
        assertThat(transferTransactions).hasSize(1);
        assertThat(transferTransactions.getFirst().getType()).isEqualTo(TransactionType.TRANSFER);

        assertThat(depositTransactions).hasSize(1);
        assertThat(depositTransactions.getFirst().getType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    @DisplayName("Should find transactions by user within time range")
    void shouldFindTransactionsByUserWithinTimeRange() {
        // Given
        Instant startTime = Instant.now().minusSeconds(3600); // 1 hour ago
        Instant endTime = Instant.now().plusSeconds(3600); // 1 hour from now

        transactionRepository.save(transferTransaction);
        transactionRepository.save(depositTransaction);
        entityManager.flush();

        // When
        List<Transaction> transactionsInRange = transactionRepository.findByUserAndTimestampBetweenOrderByTimestampDesc(
                receiverId, startTime, endTime);

        // Then
        assertThat(transactionsInRange).hasSize(2);
        assertThat(transactionsInRange).allMatch(t -> t.involvesUser(receiverId));
    }

    @Test
    @DisplayName("Should count transactions by user")
    void shouldCountTransactionsByUser() {
        // Given
        transactionRepository.save(transferTransaction);
        transactionRepository.save(depositTransaction);
        entityManager.flush();

        // When
        long senderCount = transactionRepository.countByUser(senderId);
        long receiverCount = transactionRepository.countByUser(receiverId);
        long thirdUserCount = transactionRepository.countByUser(thirdUserId);

        // Then
        assertThat(senderCount).isEqualTo(1); // Only sent the transfer
        assertThat(receiverCount).isEqualTo(2); // Received transfer and deposit
        assertThat(thirdUserCount).isZero(); // No transactions
    }

    @Test
    @DisplayName("Should delete transaction by transaction ID")
    void shouldDeleteTransactionByTransactionId() {
        // Given
        Transaction savedTransaction = transactionRepository.save(transferTransaction);
        entityManager.flush();
        TransactionId transactionId = savedTransaction.getId();

        // When
        transactionRepository.deleteByTransactionId(transactionId);
        entityManager.flush();

        // Then
        Optional<Transaction> foundTransaction = transactionRepository.findByTransactionId(transactionId);
        assertThat(foundTransaction).isEmpty();
    }

    @Test
    @DisplayName("Should handle deposit transactions correctly")
    void shouldHandleDepositTransactionsCorrectly() {
        // Given
        transactionRepository.save(depositTransaction);
        entityManager.flush();

        // When
        List<Transaction> receivedTransactions = transactionRepository.findByReceiverOrderByTimestampDesc(receiverId);
        List<Transaction> sentTransactions = transactionRepository.findBySenderOrderByTimestampDesc(receiverId);

        // Then
        assertThat(receivedTransactions).hasSize(1);
        assertThat(receivedTransactions.getFirst().getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(receivedTransactions.getFirst().getSenderId()).isNull();

        assertThat(sentTransactions).isEmpty(); // User didn't send any transactions
    }

    @Test
    @DisplayName("Should return empty list when no transactions found")
    void shouldReturnEmptyListWhenNoTransactionsFound() {
        // Given - no transactions saved

        // When
        List<Transaction> transactions = transactionRepository.findByUserOrderByTimestampDesc(senderId);

        // Then
        assertThat(transactions).isEmpty();
    }

    @Test
    @DisplayName("Should maintain transaction ordering by timestamp")
    void shouldMaintainTransactionOrderingByTimestamp() {
        // Given
        Transaction firstTransaction = Transaction.createTransfer(senderId, receiverId, Money.of(10.00));
        transactionRepository.save(firstTransaction);

        // Small delay to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            /* ignore */ }

        Transaction secondTransaction = Transaction.createTransfer(senderId, receiverId, Money.of(20.00));
        transactionRepository.save(secondTransaction);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            /* ignore */ }

        Transaction thirdTransaction = Transaction.createTransfer(senderId, receiverId, Money.of(30.00));
        transactionRepository.save(thirdTransaction);

        entityManager.flush();

        // When
        List<Transaction> transactions = transactionRepository.findByUserOrderByTimestampDesc(senderId);

        // Then
        assertThat(transactions).hasSize(3);
        // Should be ordered by timestamp descending (most recent first)
        assertThat(transactions.get(0).getAmount()).isEqualTo(Money.of(30.00));
        assertThat(transactions.get(1).getAmount()).isEqualTo(Money.of(20.00));
        assertThat(transactions.get(2).getAmount()).isEqualTo(Money.of(10.00));
    }

    @Test
    @DisplayName("Should find all transactions")
    void shouldFindAllTransactions() {
        // Given
        transactionRepository.save(transferTransaction);
        transactionRepository.save(depositTransaction);
        entityManager.flush();

        // When
        List<Transaction> allTransactions = transactionRepository.findAll();

        // Then
        assertThat(allTransactions).hasSize(2);
        assertThat(allTransactions).extracting(Transaction::getType)
                .containsExactlyInAnyOrder(TransactionType.TRANSFER, TransactionType.DEPOSIT);
    }

    @Test
    @DisplayName("Should count all transactions correctly")
    void shouldCountAllTransactionsCorrectly() {
        // Given
        assertThat(transactionRepository.count()).isZero();

        // When
        transactionRepository.save(transferTransaction);
        transactionRepository.save(depositTransaction);
        entityManager.flush();

        // Then
        assertThat(transactionRepository.count()).isEqualTo(2);
    }
}
