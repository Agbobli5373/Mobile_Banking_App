package com.mobilebanking.transaction.api.dto;

import com.mobilebanking.shared.domain.Money;import com.mobilebanking.shared.domain.TransactionId;

/**
 * Data Transfer Object for fund deposit responses.
 * Contains the transaction status, message, and deposit details.
 */
public class DepositResponse {

    private final String status;
    private final String message;
    private final DepositData data;

    private DepositResponse(String status, String message, DepositData data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * Creates a successful deposit response.
     *
     * @param transactionId the ID of the completed deposit transaction
     * @param amount        the amount that was deposited
     * @param newBalance    the user's new balance after the deposit
     * @return a successful deposit response
     */
    public static DepositResponse success(TransactionId transactionId, Money amount, Money newBalance) {
        return new DepositResponse(
                "success",
                "Funds added successfully",
                new DepositData(
                        transactionId.asString(),
                        amount.getAmount().doubleValue(),
                        newBalance.getAmount().doubleValue(),
                        "USD" // Default to USD as currency
                ));
    }

    /**
     * Creates a failure deposit response.
     *
     * @param message the error message
     * @return a failure deposit response
     */
    public static DepositResponse failure(String message) {
        return new DepositResponse("error", message, null);
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public DepositData getData() {
        return data;
    }

    /**
     * Inner class representing deposit data.
     */
    public static class DepositData {
        private final String transactionId;
        private final double amount;
        private final double newBalance;
        private final String currency;

        public DepositData(String transactionId, double amount, double newBalance, String currency) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.newBalance = newBalance;
            this.currency = currency;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public double getAmount() {
            return amount;
        }

        public double getNewBalance() {
            return newBalance;
        }

        public String getCurrency() {
            return currency;
        }
    }
}