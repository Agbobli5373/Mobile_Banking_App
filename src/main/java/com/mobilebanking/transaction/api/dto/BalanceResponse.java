package com.mobilebanking.transaction.api.dto;

import com.mobilebanking.shared.domain.Money;

/**
 * Data Transfer Object for balance responses.
 * Contains the user's current balance and currency information.
 */
public class BalanceResponse {

    private final String status;
    private final String message;
    private final BalanceData data;

    private BalanceResponse(String status, String message, BalanceData data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * Creates a successful balance response.
     *
     * @param balance the user's balance
     * @return a successful balance response
     */
    public static BalanceResponse success(Money balance) {
        return new BalanceResponse(
                "success",
                "Balance retrieved successfully",
                new BalanceData(balance.getAmount().doubleValue(), "USD")); // Default to USD as currency
    }

    /**
     * Creates a failure balance response.
     *
     * @param message the error message
     * @return a failure balance response
     */
    public static BalanceResponse failure(String message) {
        return new BalanceResponse("error", message, null);
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public BalanceData getData() {
        return data;
    }

    /**
     * Inner class representing balance data.
     */
    public static class BalanceData {
        private final double amount;
        private final String currency;

        public BalanceData(double amount, String currency) {
            this.amount = amount;
            this.currency = currency;
        }

        public double getAmount() {
            return amount;
        }

        public String getCurrency() {
            return currency;
        }
    }
}