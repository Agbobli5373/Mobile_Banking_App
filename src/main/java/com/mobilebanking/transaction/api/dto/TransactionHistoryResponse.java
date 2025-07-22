package com.mobilebanking.transaction.api.dto;

import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.transaction.domain.Transaction;
import com.mobilebanking.transaction.domain.TransactionType;
import org.springframework.data.domain.Page;

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
     * Creates a successful paginated transaction history response.
     *
     * @param transactionsPage the page of transactions
     * @param currentUserId    the ID of the current user (to determine transaction
     *                         direction)
     * @return a successful transaction history response with pagination information
     */
    public static TransactionHistoryResponse success(Page<Transaction> transactionsPage, UserId currentUserId) {
        List<TransactionDto> transactionDtos = transactionsPage.getContent().stream()
                .map(transaction -> TransactionDto.fromTransaction(transaction, currentUserId))
                .collect(Collectors.toList());

        return new TransactionHistoryResponse(
                "success",
                "Transaction history retrieved successfully",
                new TransactionHistoryData(
                        transactionDtos,
                        (int) transactionsPage.getTotalElements(),
                        transactionsPage.getNumber(),
                        transactionsPage.getSize(),
                        transactionsPage.getTotalPages(),
                        transactionsPage.isFirst(),
                        transactionsPage.isLast(),
                        transactionsPage.hasNext(),
                        transactionsPage.hasPrevious()));
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
        // Remove final modifiers to allow Jackson deserialization
        private List<TransactionDto> transactions;
        private int totalCount;
        private Integer pageNumber;
        private Integer pageSize;
        private Integer totalPages;
        private Boolean isFirstPage;
        private Boolean isLastPage;
        private Boolean hasNextPage;
        private Boolean hasPreviousPage;

        // Default constructor for Jackson deserialization
        public TransactionHistoryData() {
        }

        public TransactionHistoryData(List<TransactionDto> transactions, int totalCount) {
            this.transactions = transactions;
            this.totalCount = totalCount;
            this.pageNumber = null;
            this.pageSize = null;
            this.totalPages = null;
            this.isFirstPage = null;
            this.isLastPage = null;
            this.hasNextPage = null;
            this.hasPreviousPage = null;
        }

        public TransactionHistoryData(
                List<TransactionDto> transactions,
                int totalCount,
                int pageNumber,
                int pageSize,
                int totalPages,
                boolean isFirstPage,
                boolean isLastPage,
                boolean hasNextPage,
                boolean hasPreviousPage) {
            this.transactions = transactions;
            this.totalCount = totalCount;
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.totalPages = totalPages;
            this.isFirstPage = isFirstPage;
            this.isLastPage = isLastPage;
            this.hasNextPage = hasNextPage;
            this.hasPreviousPage = hasPreviousPage;
        }

        public List<TransactionDto> getTransactions() {
            return transactions;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public Integer getPageNumber() {
            return pageNumber;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public Integer getTotalPages() {
            return totalPages;
        }

        public Boolean getIsFirstPage() {
            return isFirstPage;
        }

        public Boolean getIsLastPage() {
            return isLastPage;
        }

        public Boolean getHasNextPage() {
            return hasNextPage;
        }

        public Boolean getHasPreviousPage() {
            return hasPreviousPage;
        }

        // Setters for Jackson deserialization
        public void setTransactions(List<TransactionDto> transactions) {
            this.transactions = transactions;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public void setPageNumber(Integer pageNumber) {
            this.pageNumber = pageNumber;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }

        public void setIsFirstPage(Boolean isFirstPage) {
            this.isFirstPage = isFirstPage;
        }

        public void setIsLastPage(Boolean isLastPage) {
            this.isLastPage = isLastPage;
        }

        public void setHasNextPage(Boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
        }

        public void setHasPreviousPage(Boolean hasPreviousPage) {
            this.hasPreviousPage = hasPreviousPage;
        }
    }

    /**
     * Inner class representing individual transaction data.
     */
    public static class TransactionDto {
        private String transactionId;
        private double amount;
        private String currency;
        private String type;
        private String direction; // "sent", "received", "deposit"
        private String counterpartyId; // The other party in the transaction (null for deposits)
        private Instant timestamp;

        // Default constructor for Jackson deserialization
        public TransactionDto() {
        }

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

        // Setters for Jackson deserialization
        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public void setCounterpartyId(String counterpartyId) {
            this.counterpartyId = counterpartyId;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }
    }
}