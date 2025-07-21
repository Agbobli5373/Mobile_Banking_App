package com.mobilebanking.shared.domain.exception;

import com.mobilebanking.shared.domain.Money;

/**
 * Exception thrown when a user attempts to perform a transaction
 * that would result in insufficient funds.
 */
public class InsufficientFundsException extends DomainException {

    private InsufficientFundsException(String message) {
        super("INSUFFICIENT_FUNDS", message);
    }

    public static InsufficientFundsException forAmount(Money requestedAmount, Money availableBalance) {
        return new InsufficientFundsException(
                String.format("Insufficient funds: requested %s, available %s",
                        requestedAmount, availableBalance));
    }

    public static InsufficientFundsException forTransfer(Money transferAmount, Money senderBalance) {
        return new InsufficientFundsException(
                String.format("Cannot transfer %s: insufficient balance of %s",
                        transferAmount, senderBalance));
    }
}