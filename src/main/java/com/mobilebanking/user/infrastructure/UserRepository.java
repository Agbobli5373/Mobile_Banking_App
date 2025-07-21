package com.mobilebanking.user.infrastructure;

import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User aggregate with domain-focused methods.
 * Provides persistence operations for User entities with custom queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Finds a user by their unique identifier.
     * 
     * @param userId the user ID to search for
     * @return Optional containing the user if found, empty otherwise
     */
    default Optional<User> findByUserId(UserId userId) {
        return findById(userId.asString());
    }

    /**
     * Finds a user by their phone number.
     * 
     * @param phoneNumber the phone number to search for
     * @return Optional containing the user if found, empty otherwise
     */
    @Query("SELECT u FROM User u WHERE u.phone.value = :phoneNumber")
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Finds a user by their phone number using domain object.
     * 
     * @param phone the phone number domain object to search for
     * @return Optional containing the user if found, empty otherwise
     */
    default Optional<User> findByPhone(PhoneNumber phone) {
        return findByPhoneNumber(phone.getValue());
    }

    /**
     * Checks if a user exists with the given phone number.
     * 
     * @param phoneNumber the phone number to check
     * @return true if a user exists with this phone number, false otherwise
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.phone.value = :phoneNumber")
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Checks if a user exists with the given phone number using domain object.
     * 
     * @param phone the phone number domain object to check
     * @return true if a user exists with this phone number, false otherwise
     */
    default boolean existsByPhone(PhoneNumber phone) {
        return existsByPhoneNumber(phone.getValue());
    }

    /**
     * Checks if a user exists with the given user ID.
     * 
     * @param userId the user ID to check
     * @return true if a user exists with this ID, false otherwise
     */
    default boolean existsByUserId(UserId userId) {
        return existsById(userId.asString());
    }

    /**
     * Deletes a user by their unique identifier.
     * 
     * @param userId the user ID to delete
     */
    default void deleteByUserId(UserId userId) {
        deleteById(userId.asString());
    }
}