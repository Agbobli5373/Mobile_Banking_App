package com.mobilebanking.transaction.api.dto;

import jakarta.validation.constraints.Positive;

/**
 * Data Transfer Object for fund deposit requests.
 * Contains the amount to deposit to the user's wallet.
 */
public class DepositRequest {

    @Positive(message = "Deposit amount must be positive")
    private double amount;

    // Default constructor for JSON deserialization
    public DepositRequest() {
    }

    public DepositRequest(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "DepositRequest{" +
                "amount=" + amount +
                '}';
    }
}