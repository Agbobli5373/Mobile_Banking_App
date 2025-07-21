package com.mobilebanking.transaction.domain;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.shared.domain.exception.InsufficientFundsException;
import org.springframework.stereotype.Service;
import java.util.Objects;

/**
 * MoneyTransferService domain service that encapsulates the business logic
 * for money transfers between users, including validation and business rules.
 */
@Service
public class MoneyTransferService {

    /**
     * Validates and creates a money transfer transaction between two users.
     * This method encapsulates all the business rules for money transfers.
     * 
     * @param senderId      the ID of the user sending money
     * @param receiverId    the ID of the user receiving money
     * @param amount        the amount to transfer
     * @param senderBalance the current balance of the sender
     * @return a new Transaction representing the transfer
     * @throws IllegalArgumentException   if validation fails
     * @throws InsufficientFundsException if sender doesn't have enough balance
     */
    public Transaction createTransfer(UserId senderId, UserId receiverId, Money amount, Money senderBalance) {
        validateTransferRequest(senderId, receiverId, amount, senderBalance);
        return Transaction.createTransfer(senderId, receiverId, amount);
    }

    /**
     * Validates a money transfer request according to business rules.
     * 
     * @param senderId      the ID of the user sending money
     * @param receiverId    the ID of the user receiving money
     * @param amount        the amount to transfer
     * @param senderBalance the current balance of the sender
     * @throws IllegalArgumentException   if basic validation fails
     * @throws InsufficientFundsException if sender doesn't have enough balance
     */
    public void validateTransferRequest(UserId senderId, UserId receiverId, Money amount, Money senderBalance) {
        // Null checks
        Objects.requireNonNull(senderId, "Sender ID cannot be null");
        Objects.requireNonNull(receiverId, "Receiver ID cannot be null");
        Objects.requireNonNull(amount, "Transfer amount cannot be null");
        Objects.requireNonNull(senderBalance, "Sender balance cannot be null");

        // Business rule: Cannot transfer to yourself
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot transfer money to yourself");
        }

        // Business rule: Transfer amount must be positive
        if (amount.isZero()) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        // Business rule: Sender must have sufficient balance
        if (!senderBalance.isGreaterThanOrEqual(amount)) {
            throw InsufficientFundsException.forTransfer(amount, senderBalance);
        }
    }

    /**
     * Validates if a user has sufficient balance for a transfer.
     * 
     * @param balance the user's current balance
     * @param amount  the amount to transfer
     * @return true if the user has sufficient balance
     * @throws IllegalArgumentException if parameters are null
     */
    public boolean hasSufficientBalance(Money balance, Money amount) {
        Objects.requireNonNull(balance, "Balance cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");

        return balance.isGreaterThanOrEqual(amount);
    }

    /**
     * Calculates the new balance after a debit operation.
     * 
     * @param currentBalance the current balance
     * @param debitAmount    the amount to debit
     * @return the new balance after debit
     * @throws IllegalArgumentException   if parameters are null
     * @throws InsufficientFundsException if insufficient balance
     */
    public Money calculateBalanceAfterDebit(Money currentBalance, Money debitAmount) {
        Objects.requireNonNull(currentBalance, "Current balance cannot be null");
        Objects.requireNonNull(debitAmount, "Debit amount cannot be null");

        if (!currentBalance.isGreaterThanOrEqual(debitAmount)) {
            throw InsufficientFundsException.forAmount(debitAmount, currentBalance);
        }

        return currentBalance.subtract(debitAmount);
    }

    /**
     * Calculates the new balance after a credit operation.
     * 
     * @param currentBalance the current balance
     * @param creditAmount   the amount to credit
     * @return the new balance after credit
     * @throws IllegalArgumentException if parameters are null
     */
    public Money calculateBalanceAfterCredit(Money currentBalance, Money creditAmount) {
        Objects.requireNonNull(currentBalance, "Current balance cannot be null");
        Objects.requireNonNull(creditAmount, "Credit amount cannot be null");

        return currentBalance.add(creditAmount);
    }

    /**
     * Validates if a transfer amount is within acceptable limits.
     * This method can be extended to include business rules like daily limits, etc.
     * 
     * @param amount the transfer amount to validate
     * @return true if the amount is within acceptable limits
     * @throws IllegalArgumentException if amount is null
     */
    public boolean isValidTransferAmount(Money amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");

        // Basic validation - amount must be positive
        return !amount.isZero();

        // Future: Add business rules for maximum transfer limits
        // For now, any positive amount is valid
    }
}