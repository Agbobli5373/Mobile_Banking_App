package com.mobilebanking.user.infrastructure.repository;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.domain.UserName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for UserRepository to verify database operations.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserJpaRepository userRepository;

    @Test
    void shouldSaveAndRetrieveUser() {
        // Given
        User user = User.create(
                new UserName("Test User"),
                new PhoneNumber("1234567890"),
                "1234");
        user.creditBalance(Money.of(new BigDecimal("100.00")));

        // When
        User savedUser = userRepository.save(user);
        Optional<User> retrievedUser = userRepository.findById(user.getId().asString());

        // Then
        assertTrue(retrievedUser.isPresent());
        assertEquals(user.getId(), retrievedUser.get().getId());
        assertEquals(user.getName().getValue(), retrievedUser.get().getName().getValue());
        assertEquals(user.getPhone().getValue(), retrievedUser.get().getPhone().getValue());
        assertEquals(0, user.getBalance().getAmount().compareTo(retrievedUser.get().getBalance().getAmount()));
    }

    @Test
    void shouldFindUserByPhone() {
        // Given
        String phoneNumber = "9876543210";
        User user = User.create(
                new UserName("Phone Test User"),
                new PhoneNumber(phoneNumber),
                "5678");
        userRepository.save(user);

        // When
        Optional<User> retrievedUser = userRepository.findByPhone(phoneNumber);

        // Then
        assertTrue(retrievedUser.isPresent());
        assertEquals(phoneNumber, retrievedUser.get().getPhone().getValue());
    }

    @Test
    void shouldUpdateUserBalance() {
        // Given
        User user = User.create(
                new UserName("Balance Test User"),
                new PhoneNumber("5555555555"),
                "9999");
        user.creditBalance(Money.of(new BigDecimal("200.00")));
        User savedUser = userRepository.save(user);

        // When
        savedUser.debitBalance(Money.of(new BigDecimal("50.00")));
        userRepository.save(savedUser);
        Optional<User> retrievedUser = userRepository.findById(user.getId().asString());

        // Then
        assertTrue(retrievedUser.isPresent());
        assertEquals(0, new BigDecimal("150.00").compareTo(retrievedUser.get().getBalance().getAmount()));
    }
}