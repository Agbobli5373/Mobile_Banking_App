package com.mobilebanking.transaction.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Data Transfer Object for money transfer requests.
 * Contains the recipient's phone number and the amount to transfer.
 */
public class TransferRequest {

    @NotBlank(message = "Recipient phone number is required")
    private String recipientPhone;

    @Positive(message = "Transfer amount must be positive")
    private double amount;

    // Default constructor for JSON deserialization
    public TransferRequest() {
    }

    public TransferRequest(String recipientPhone, double amount) {
        this.recipientPhone = recipientPhone;
        this.amount = amount;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "TransferRequest{" +
                "recipientPhone='" + recipientPhone + '\'' +
                ", amount=" + amount +
                '}';
    }
}