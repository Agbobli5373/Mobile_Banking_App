package com.mobilebanking.user.infrastructure.repository;

import com.mobilebanking.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for User entity.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User, String> {

    /**
     * Find a user by phone number.
     *
     * @param phone the phone number
     * @return optional user
     */
    @Query("SELECT u FROM User u WHERE u.phone.value = :phone")
    Optional<User> findByPhone(@Param("phone") String phone);

    /**
     * Check if a phone number is already registered.
     *
     * @param phone the phone number
     * @return true if the phone number exists
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.phone.value = :phone")
    boolean existsByPhone(@Param("phone") String phone);
}