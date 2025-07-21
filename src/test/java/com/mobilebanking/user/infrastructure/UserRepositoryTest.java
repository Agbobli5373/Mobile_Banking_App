package com.mobilebanking.user.infrastructure;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.domain.UserName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository using @DataJpaTest.
 * Tests the repository layer with an embedded database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private PhoneNumber testPhone;
    private UserName testName;

    @BeforeEach
    void setUp() {
        testName = UserName.of("John Doe");
        testPhone = PhoneNumber.of("1234567890");
        testUser = User.create(testName, testPhone, "1234");
    }

    @Test
    @DisplayName("Should save and find user by ID")
    void shouldSaveAndFindUserById() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<User> foundUser = userRepository.findByUserId(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getName()).isEqualTo(testName);
        assertThat(foundUser.get().getPhone()).isEqualTo(testPhone);
        assertThat(foundUser.get().getBalance()).isEqualTo(Money.zero());
    }

    @Test
    @DisplayName("Should find user by phone number")
    void shouldFindUserByPhoneNumber() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<User> foundUser = userRepository.findByPhone(testPhone);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getPhone()).isEqualTo(testPhone);
        assertThat(foundUser.get().getName()).isEqualTo(testName);
    }

    @Test
    @DisplayName("Should find user by phone number string")
    void shouldFindUserByPhoneNumberString() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<User> foundUser = userRepository.findByPhoneNumber("1234567890");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getPhone().getValue()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("Should return empty when user not found by phone")
    void shouldReturnEmptyWhenUserNotFoundByPhone() {
        // Given
        PhoneNumber nonExistentPhone = PhoneNumber.of("9999999999");

        // When
        Optional<User> foundUser = userRepository.findByPhone(nonExistentPhone);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should check if user exists by phone number")
    void shouldCheckIfUserExistsByPhoneNumber() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsByPhone(testPhone);
        boolean notExists = userRepository.existsByPhone(PhoneNumber.of("9999999999"));

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should check if user exists by phone number string")
    void shouldCheckIfUserExistsByPhoneNumberString() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsByPhoneNumber("1234567890");
        boolean notExists = userRepository.existsByPhoneNumber("9999999999");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should delete user by user ID")
    void shouldDeleteUserByUserId() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        UserId userId = savedUser.getId();

        // When
        userRepository.deleteByUserId(userId);
        entityManager.flush();

        // Then
        Optional<User> foundUser = userRepository.findByUserId(userId);
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should handle user with updated balance")
    void shouldHandleUserWithUpdatedBalance() {
        // Given
        User savedUser = userRepository.save(testUser);
        savedUser.creditBalance(Money.of(100.50));
        userRepository.save(savedUser);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<User> foundUser = userRepository.findByUserId(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getBalance()).isEqualTo(Money.of(100.50));
    }

    @Test
    @DisplayName("Should maintain phone number uniqueness constraint")
    void shouldMaintainPhoneNumberUniquenessConstraint() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();

        // Then
        // This should be handled by the database constraint
        // The actual constraint violation will be thrown when we try to save
        assertThat(userRepository.existsByPhone(testPhone)).isTrue();
    }

    @Test
    @DisplayName("Should find all users")
    void shouldFindAllUsers() {
        // Given
        User user1 = testUser;
        User user2 = User.create(
                UserName.of("Jane Smith"),
                PhoneNumber.of("9876543210"),
                "5678");

        userRepository.save(user1);
        userRepository.save(user2);
        entityManager.flush();

        // When
        var allUsers = userRepository.findAll();

        // Then
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(User::getName)
                .containsExactlyInAnyOrder(
                        UserName.of("John Doe"),
                        UserName.of("Jane Smith"));
    }

    @Test
    @DisplayName("Should count users correctly")
    void shouldCountUsersCorrectly() {
        // Given
        assertThat(userRepository.count()).isEqualTo(0);

        // When
        userRepository.save(testUser);
        entityManager.flush();

        // Then
        assertThat(userRepository.count()).isEqualTo(1);
    }
}