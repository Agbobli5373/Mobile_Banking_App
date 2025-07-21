package com.mobilebanking.transaction.infrastructure;

import com.mobilebanking.shared.domain.TransactionId;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.transaction.domain.Transaction;
import com.mobilebanking.transaction.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction aggregate with domain-focused methods.
 * Provides persistence operations for Transaction entities with custom queries
 * for transaction history.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Finds a transaction by its unique identifier.
     * 
     * @param transactionId the transaction ID to search for
     * @return Optional containing the transaction if found, empty otherwise
     */
    default Optional<Transaction> findByTransactionId(TransactionId transactionId) {
        return findById(transactionId.asString());
    }

    /**
     * Finds all transactions where the user is either sender or receiver, ordered
     * by timestamp descending.
     * 
     * @param userId the user ID to search for
     * @return List of transactions involving the user, ordered by most recent first
     */
    @Query("SELECT t FROM Transaction t WHERE t.senderId = :userId OR t.receiverId = :userId ORDER BY t.timestamp.timestamp DESC")
    List<Transaction> findByUserIdOrderByTimestampDesc(@Param("userId") String userId);

    /**
     * Finds all transactions where the user is either sender or receiver using
     * domain object.
     * 
     * @param userId the user ID domain object to search for
     * @return List of transactions involving the user, ordered by most recent first
     */
    default List<Transaction> findByUserOrderByTimestampDesc(UserId userId) {
        return findByUserIdOrderByTimestampDesc(userId.asString());
    }

    /**
     * Finds all transactions sent by a specific user, ordered by timestamp
     * descending.
     * 
     * @param senderId the sender user ID to search for
     * @return List of transactions sent by the user, ordered by most recent first
     */
    @Query("SELECT t FROM Transaction t WHERE t.senderId = :senderId ORDER BY t.timestamp.timestamp DESC")
    List<Transaction> findBySenderIdOrderByTimestampDesc(@Param("senderId") String senderId);

    /**
     * Finds all transactions sent by a specific user using domain object.
     * 
     * @param senderId the sender user ID domain object to search for
     * @return List of transactions sent by the user, ordered by most recent first
     */
    default List<Transaction> findBySenderOrderByTimestampDesc(UserId senderId) {
        return findBySenderIdOrderByTimestampDesc(senderId.asString());
    }

    /**
     * Finds all transactions received by a specific user, ordered by timestamp
     * descending.
     * 
     * @param receiverId the receiver user ID to search for
     * @return List of transactions received by the user, ordered by most recent
     *         first
     */
    @Query("SELECT t FROM Transaction t WHERE t.receiverId = :receiverId ORDER BY t.timestamp.timestamp DESC")
    List<Transaction> findByReceiverIdOrderByTimestampDesc(@Param("receiverId") String receiverId);

    /**
     * Finds all transactions received by a specific user using domain object.
     * 
     * @param receiverId the receiver user ID domain object to search for
     * @return List of transactions received by the user, ordered by most recent
     *         first
     */
    default List<Transaction> findByReceiverOrderByTimestampDesc(UserId receiverId) {
        return findByReceiverIdOrderByTimestampDesc(receiverId.asString());
    }

    /**
     * Finds all transactions of a specific type involving a user, ordered by
     * timestamp descending.
     * 
     * @param userId the user ID to search for
     * @param type   the transaction type to filter by
     * @return List of transactions of the specified type involving the user
     */
    @Query("SELECT t FROM Transaction t WHERE (t.senderId = :userId OR t.receiverId = :userId) AND t.type = :type ORDER BY t.timestamp.timestamp DESC")
    List<Transaction> findByUserIdAndTypeOrderByTimestampDesc(@Param("userId") String userId,
            @Param("type") TransactionType type);

    /**
     * Finds all transactions of a specific type involving a user using domain
     * objects.
     * 
     * @param userId the user ID domain object to search for
     * @param type   the transaction type to filter by
     * @return List of transactions of the specified type involving the user
     */
    default List<Transaction> findByUserAndTypeOrderByTimestampDesc(UserId userId, TransactionType type) {
        return findByUserIdAndTypeOrderByTimestampDesc(userId.asString(), type);
    }

    /**
     * Finds all transactions involving a user within a specific time range, ordered
     * by timestamp descending.
     * 
     * @param userId    the user ID to search for
     * @param startTime the start of the time range
     * @param endTime   the end of the time range
     * @return List of transactions within the time range involving the user
     */
    @Query("SELECT t FROM Transaction t WHERE (t.senderId = :userId OR t.receiverId = :userId) AND t.timestamp.timestamp BETWEEN :startTime AND :endTime ORDER BY t.timestamp.timestamp DESC")
    List<Transaction> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            @Param("userId") String userId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    /**
     * Finds all transactions involving a user within a specific time range using
     * domain objects.
     * 
     * @param userId    the user ID domain object to search for
     * @param startTime the start of the time range
     * @param endTime   the end of the time range
     * @return List of transactions within the time range involving the user
     */
    default List<Transaction> findByUserAndTimestampBetweenOrderByTimestampDesc(
            UserId userId, Instant startTime, Instant endTime) {
        return findByUserIdAndTimestampBetweenOrderByTimestampDesc(userId.asString(), startTime, endTime);
    }

    /**
     * Counts the total number of transactions involving a specific user.
     * 
     * @param userId the user ID to count transactions for
     * @return the total number of transactions involving the user
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.senderId = :userId OR t.receiverId = :userId")
    long countByUserId(@Param("userId") String userId);

    /**
     * Counts the total number of transactions involving a specific user using
     * domain object.
     * 
     * @param userId the user ID domain object to count transactions for
     * @return the total number of transactions involving the user
     */
    default long countByUser(UserId userId) {
        return countByUserId(userId.asString());
    }

    /**
     * Deletes a transaction by its unique identifier.
     * 
     * @param transactionId the transaction ID to delete
     */
    default void deleteByTransactionId(TransactionId transactionId) {
        deleteById(transactionId.asString());
    }
}