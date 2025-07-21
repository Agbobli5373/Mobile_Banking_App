package com.mobilebanking.transaction.api.dto;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.TransactionId;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.transaction.domain.Transaction;
import com.mobilebanking.transaction.domain.TransactionType;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for transaction history responses.
 * Contains a list of transactions for the authenticated user.
 */
public class TransactionHistoryResponse {

    private final String status;
    private final String message;
    private final TransactionHistoryData data;

    private TransactionHistoryResponse(String status, String message, TransactionHistoryData data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * Creates a successful transaction history response.
     *
     * @param transactions  the list of transactions
     * @param currentUserId the ID of the current user (to determine transaction
     *                      direction)
     * @return a successful transaction history response
     */
    public static TransactionHistoryResponse success(List<Transaction> transactions, UserId currentUserId) {
        List<TransactionDto> transactionDtos = transactions.stream()
                .map(transaction -> TransactionDto.fromTransaction(transaction, currentUserId))
                .collect(Collectors.toList());

        return new TransactionHistoryResponse(
                "success",
                "Transaction history retrieved successfully",
                new TransactionHistoryData(transactionDtos, transactionDtos.size()));
    }

    /**
     * Creates a failure transaction history response.
     *
     * @param message the error message
     * @return a failure transaction history response
     */
    public static TransactionHistoryResponse failure(String message) {
        return new TransactionHistoryResponse("error", message, null);
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public TransactionHistoryData getData() {
        return data;
    }

    /**
     * Inner class representing transaction history data.
     */
    public static class TransactionHistoryData {
        private final List<TransactionDto> transactions;
        private final int totalCount;

        public TransactionHistoryData(List<TransactionDto> transactions, int totalCount) {
            this.transactions = transactions;
            this.totalCount = totalCount;
        }

        public List<TransactionDto> getTransactions() {
            return transactions;
        }

        public int getTotalCount() {
            return totalCount;
        }
    }

    /**
     * Inner class representing individual transaction data.
     */
    public static class TransactionDto {
        private final String transactionId;
        private final double amount;
        private final String currency;
        private final String type;
        private final String direction; // "sent", "received", "deposit"
        private final String counterpartyId; // The other party in the transaction (null for deposits)
        private final Instant timestamp;

        public TransactionDto(String transactionId, double amount, String currency, String type,
                String direction, String counterpartyId, Instant timestamp) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.currency = currency;
            this.type = type;
            this.direction = direction;
            this.counterpartyId = counterpartyId;
            this.timestamp = timestamp;
        }

        /**
         * Creates a TransactionDto from a Transaction domain object.
         *
         * @param transaction   the transaction domain object
         * @param currentUserId the current user's ID to determine direction
         * @return TransactionDto representation
         */
        public static TransactionDto fromTransaction(Transaction transaction, UserId currentUserId) {
            String direction;
            String counterpartyId = null;

            if (transaction.getType() == TransactionType.DEPOSIT) {
                direction = "deposit";
                // No counterparty for deposits
            } else if (transaction.getType() == TransactionType.TRANSFER) {
                if (currentUserId.equals(transaction.getSenderId())) {
                    direction = "sent";
                    counterpartyId = transaction.getReceiverId().asString();
                } else {
                    direction = "received";
                    counterpartyId = transaction.getSenderId().asString();
                }
            } else {
                direction = "unknown";
            }

            return new TransactionDto(
                    transaction.getId().asString(),
                    transaction.getAmount().getAmount().doubleValue(),
                    "USD", // Default currency
                    transaction.getType().name().toLowerCase(),
                    direction,
                    counterpartyId,
                    transaction.getTimestamp().getValue());
        }

        public String getTransactionId() {
            return transactionId;
        }

        public double getAmount() {
            return amount;
        }

        public String getCurrency() {
            return currency;
        }

        public String getType() {
            return type;
        }

        public String getDirection() {
            return direction;
        }

        public String getCounterpartyId() {
            return counterpartyId;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }
}