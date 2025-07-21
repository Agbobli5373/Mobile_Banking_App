package com.mobilebanking.transaction.api.dto;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.TransactionId;

/**
 * Data Transfer Object for money transfer responses.
 * Contains the transaction status, message, and transaction details.
 */
public class TransferResponse {

    private final String status;
    private final String message;
    private final TransferData data;

    private TransferResponse(String status, String message, TransferData data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * Creates a successful transfer response.
     *
     * @param transactionId  the ID of the completed transaction
     * @param amount         the amount that was transferred
     * @param recipientPhone the recipient's phone number
     * @param newBalance     the sender's new balance after the transfer
     * @return a successful transfer response
     */
    public static TransferResponse success(TransactionId transactionId, Money amount,
            String recipientPhone, Money newBalance) {
        return new TransferResponse(
                "success",
                "Transfer completed successfully",
                new TransferData(
                        transactionId.asString(),
                        amount.getAmount().doubleValue(),
                        recipientPhone,
                        newBalance.getAmount().doubleValue(),
                        "USD" // Default to USD as currency
                ));
    }

    /**
     * Creates a failure transfer response.
     *
     * @param message the error message
     * @return a failure transfer response
     */
    public static TransferResponse failure(String message) {
        return new TransferResponse("error", message, null);
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public TransferData getData() {
        return data;
    }

    /**
     * Inner class representing transfer data.
     */
    public static class TransferData {
        private final String transactionId;
        private final double amount;
        private final String recipientPhone;
        private final double newBalance;
        private final String currency;

        public TransferData(String transactionId, double amount, String recipientPhone,
                double newBalance, String currency) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.recipientPhone = recipientPhone;
            this.newBalance = newBalance;
            this.currency = currency;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public double getAmount() {
            return amount;
        }

        public String getRecipientPhone() {
            return recipientPhone;
        }

        public double getNewBalance() {
            return newBalance;
        }

        public String getCurrency() {
            return currency;
        }
    }
}