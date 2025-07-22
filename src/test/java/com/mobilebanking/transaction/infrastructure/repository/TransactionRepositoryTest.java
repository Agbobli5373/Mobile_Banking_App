package com.mobilebanking.transaction.infrastructure.repository;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.transaction.domain.Transaction;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.domain.UserName;
import com.mobilebanking.user.infrastructure.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for TransactionRepository to verify database operations.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class TransactionRepositoryTest {

    @Autowired
    private TransactionJpaRepository transactionRepository;

    @Autowired
    private UserJpaRepository userRepository;

    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        // Create test users
        sender = User.create(
                new UserName("Sender User"),
                new PhoneNumber("1111111111"),
                "1111");
        sender.creditBalance(Money.of(new BigDecimal("1000.00")));
        userRepository.save(sender);

        receiver = User.create(
                new UserName("Receiver User"),
                new PhoneNumber("2222222222"),
                "2222");
        userRepository.save(receiver);
    }

    @Test
    void shouldSaveAndRetrieveTransaction() {
        // Given
        Transaction transaction = Transaction.createTransfer(
                sender.getId(),
                receiver.getId(),
                Money.of(new BigDecimal("100.00")));

        // When
        Transaction savedTransaction = transactionRepository.save(transaction);
        Optional<Transaction> retrievedTransaction = transactionRepository.findById(transaction.getId().asString());

        // Then
        assertTrue(retrievedTransaction.isPresent());
        assertEquals(transaction.getId(), retrievedTransaction.get().getId());
        assertEquals(transaction.getSenderId(), retrievedTransaction.get().getSenderId());
        assertEquals(transaction.getReceiverId(), retrievedTransaction.get().getReceiverId());
        assertEquals(0,
                transaction.getAmount().getAmount().compareTo(retrievedTransaction.get().getAmount().getAmount()));
    }

    @Test
    void shouldFindTransactionsByUserId() {
        // Given
        UserId senderId = sender.getId();
        UserId receiverId = receiver.getId();

        // Create multiple transactions
        Transaction transaction1 = Transaction.createTransfer(senderId, receiverId, Money.of(new BigDecimal("50.00")));
        Transaction transaction2 = Transaction.createTransfer(senderId, receiverId, Money.of(new BigDecimal("75.00")));
        Transaction transaction3 = Transaction.createDeposit(receiverId, Money.of(new BigDecimal("200.00")));

        transactionRepository.saveAll(List.of(transaction1, transaction2, transaction3));

        // When
        List<Transaction> senderTransactions = transactionRepository.findBySenderId(senderId.asString());
        List<Transaction> receiverTransactions = transactionRepository.findByReceiverId(receiverId.asString());
        Page<Transaction> userTransactions = transactionRepository.findByUserInvolved(
                senderId.asString(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp")));

        // Then
        assertEquals(2, senderTransactions.size());
        assertEquals(3, receiverTransactions.size()); // 2 transfers + 1 deposit
        assertEquals(2, userTransactions.getContent().size());
    }

    @Test
    void shouldOrderTransactionsByTimestampDesc() {
        // Given
        UserId senderId = sender.getId();
        UserId receiverId = receiver.getId();

        // Create transactions with slight delay between them
        Transaction transaction1 = Transaction.createTransfer(senderId, receiverId, Money.of(new BigDecimal("10.00")));
        try {
            Thread.sleep(100); // Small delay to ensure different timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Transaction transaction2 = Transaction.createTransfer(senderId, receiverId, Money.of(new BigDecimal("20.00")));
        try {
            Thread.sleep(100); // Small delay to ensure different timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Transaction transaction3 = Transaction.createTransfer(senderId, receiverId, Money.of(new BigDecimal("30.00")));

        transactionRepository.saveAll(List.of(transaction1, transaction2, transaction3));

        // When
        Page<Transaction> transactions = transactionRepository.findByUserInvolved(
                senderId.asString(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp")));

        // Then
        assertEquals(3, transactions.getContent().size());
        assertEquals(transaction3.getId(), transactions.getContent().get(0).getId()); // Most recent first
        assertEquals(transaction2.getId(), transactions.getContent().get(1).getId());
        assertEquals(transaction1.getId(), transactions.getContent().get(2).getId());
    }
}