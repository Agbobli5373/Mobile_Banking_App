package com.mobilebanking.transaction.domain;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.TransactionId;
import com.mobilebanking.shared.domain.UserId;
import jakarta.persistence.*;
import java.util.Objects;

/**
 * Transaction aggregate root representing a financial transaction in the mobile
 * banking system.
 * Encapsulates all transaction-related business logic and maintains
 * consistency.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "id")
    private String id; // Store UUID as string for JPA

    @Column(name = "sender_id")
    private String senderId; // Can be null for deposits

    @Column(name = "receiver_id", nullable = false)
    private String receiverId;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "amount", precision = 19, scale = 2, nullable = false))
    private Money amount;

    @Embedded
    @AttributeOverride(name = "timestamp", column = @Column(name = "timestamp", nullable = false))
    private TransactionTimestamp timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType type;

    // JPA requires default constructor
    protected Transaction() {
    }

    private Transaction(TransactionId id, UserId senderId, UserId receiverId,
            Money amount, TransactionTimestamp timestamp, TransactionType type) {
        this.id = Objects.requireNonNull(id, "Transaction ID cannot be null").asString();
        this.senderId = senderId != null ? senderId.asString() : null; // Can be null for deposits
        this.receiverId = Objects.requireNonNull(receiverId, "Receiver ID cannot be null").asString();
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.type = Objects.requireNonNull(type, "Transaction type cannot be null");

        validateTransaction();
    }

    /**
     * Factory method to create a money transfer transaction between two users.
     * 
     * @param sender   the user sending money
     * @param receiver the user receiving money
     * @param amount   the amount to transfer
     * @return new Transaction instance for transfer
     * @throws IllegalArgumentException if sender equals receiver or any parameter
     *                                  is invalid
     */
    public static Transaction createTransfer(UserId sender, UserId receiver, Money amount) {
        if (sender == null) {
            throw new IllegalArgumentException("Sender cannot be null for transfer");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver cannot be null for transfer");
        }
        if (sender.equals(receiver)) {
            throw new IllegalArgumentException("Cannot transfer money to yourself");
        }
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        return new Transaction(
                TransactionId.generate(),
                sender,
                receiver,
                amount,
                TransactionTimestamp.now(),
                TransactionType.TRANSFER);
    }

    /**
     * Factory method to create a deposit transaction for adding funds to a user's
     * wallet.
     * 
     * @param user   the user receiving the deposit
     * @param amount the amount to deposit
     * @return new Transaction instance for deposit
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static Transaction createDeposit(UserId user, Money amount) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null for deposit");
        }
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        return new Transaction(
                TransactionId.generate(),
                null, // No sender for deposits
                user,
                amount,
                TransactionTimestamp.now(),
                TransactionType.DEPOSIT);
    }

    /**
     * Validates the transaction based on its type and properties.
     */
    private void validateTransaction() {
        switch (type) {
            case TRANSFER:
                if (senderId == null) {
                    throw new IllegalArgumentException("Transfer transactions must have a sender");
                }
                if (senderId.equals(receiverId)) {
                    throw new IllegalArgumentException("Cannot transfer to yourself");
                }
                break;
            case DEPOSIT:
                if (senderId != null) {
                    throw new IllegalArgumentException("Deposit transactions should not have a sender");
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown transaction type: " + type);
        }
    }

    /**
     * Checks if this transaction involves the specified user as either sender or
     * receiver.
     * 
     * @param userId the user ID to check
     * @return true if the user is involved in this transaction
     */
    public boolean involvesUser(UserId userId) {
        if (userId == null) {
            return false;
        }
        String userIdString = userId.asString();
        return userIdString.equals(senderId) || userIdString.equals(receiverId);
    }

    /**
     * Checks if this is a transfer transaction.
     * 
     * @return true if this is a transfer transaction
     */
    public boolean isTransfer() {
        return type == TransactionType.TRANSFER;
    }

    /**
     * Checks if this is a deposit transaction.
     * 
     * @return true if this is a deposit transaction
     */
    public boolean isDeposit() {
        return type == TransactionType.DEPOSIT;
    }

    // Getters
    public TransactionId getId() {
        return TransactionId.fromString(id);
    }

    public UserId getSenderId() {
        return senderId != null ? UserId.fromString(senderId) : null;
    }

    public UserId getReceiverId() {
        return UserId.fromString(receiverId);
    }

    public Money getAmount() {
        return amount;
    }

    public TransactionTimestamp getTimestamp() {
        return timestamp;
    }

    public TransactionType getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Transaction that = (Transaction) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", type=" + type +
                '}';
    }
}