package com.mobilebanking.transaction.infrastructure.repository;

import com.mobilebanking.transaction.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA repository for Transaction entity.
 */
@Repository
public interface TransactionJpaRepository extends JpaRepository<Transaction, String> {

    /**
     * Find all transactions where the specified user is the sender.
     *
     * @param senderId the sender user ID
     * @return list of transactions
     */
    List<Transaction> findBySenderId(String senderId);

    /**
     * Find all transactions where the specified user is the receiver.
     *
     * @param receiverId the receiver user ID
     * @return list of transactions
     */
    List<Transaction> findByReceiverId(String receiverId);

    /**
     * Find all transactions where the specified user is either sender or receiver.
     *
     * @param userId the user ID
     * @return list of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.senderId = :userId OR t.receiverId = :userId")
    List<Transaction> findByUserInvolved(@Param("userId") String userId);

    /**
     * Find all transactions where the specified user is either sender or receiver,
     * with pagination and sorting.
     *
     * @param userId   the user ID
     * @param pageable pagination and sorting information
     * @return page of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.senderId = :userId OR t.receiverId = :userId")
    Page<Transaction> findByUserInvolved(@Param("userId") String userId, Pageable pageable);

    /**
     * Find all transactions between the specified date range.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @param pageable  pagination and sorting information
     * @return page of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.timestamp.timestamp BETWEEN :startDate AND :endDate")
    Page<Transaction> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find all transactions for a user within the specified date range.
     *
     * @param userId    the user ID
     * @param startDate the start date
     * @param endDate   the end date
     * @param pageable  pagination and sorting information
     * @return page of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE (t.senderId = :userId OR t.receiverId = :userId) " +
            "AND t.timestamp.timestamp BETWEEN :startDate AND :endDate")
    Page<Transaction> findByUserAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}