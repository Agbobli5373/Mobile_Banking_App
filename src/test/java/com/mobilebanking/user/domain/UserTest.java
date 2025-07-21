package com.mobilebanking.user.domain;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.shared.domain.exception.InsufficientFundsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Aggregate Tests")
class UserTest {

    private UserName validName;
    private PhoneNumber validPhone;
    private String validPin;

    @BeforeEach
    void setUp() {
        validName = UserName.of("John Doe");
        validPhone = PhoneNumber.of("1234567890");
        validPin = "1234";
    }

    @Test
    @DisplayName("Should create new User with valid details")
    void shouldCreateNewUserWithValidDetails() {
        // When
        User user = User.create(validName, validPhone, validPin);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isEqualTo(validName);
        assertThat(user.getPhone()).isEqualTo(validPhone);
        assertThat(user.getBalance()).isEqualTo(Money.zero());
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should validate correct PIN")
    void shouldValidateCorrectPin() {
        // Given
        User user = User.create(validName, validPhone, validPin);

        // When & Then
        assertThat(user.hasValidPin(validPin)).isTrue();
    }

    @Test
    @DisplayName("Should reject incorrect PIN")
    void shouldRejectIncorrectPin() {
        // Given
        User user = User.create(validName, validPhone, validPin);

        // When & Then
        assertThat(user.hasValidPin("5678")).isFalse();
    }

    @Test
    @DisplayName("Should credit balance successfully")
    void shouldCreditBalanceSuccessfully() {
        // Given
        User user = User.create(validName, validPhone, validPin);
        Money creditAmount = Money.of(BigDecimal.valueOf(100.00));

        // When
        user.creditBalance(creditAmount);

        // Then
        assertThat(user.getBalance()).isEqualTo(creditAmount);
    }

    @Test
    @DisplayName("Should debit balance when sufficient funds available")
    void shouldDebitBalanceWhenSufficientFundsAvailable() {
        // Given
        User user = User.create(validName, validPhone, validPin);
        Money initialAmount = Money.of(BigDecimal.valueOf(100.00));
        Money debitAmount = Money.of(BigDecimal.valueOf(50.00));
        Money expectedBalance = Money.of(BigDecimal.valueOf(50.00));

        user.creditBalance(initialAmount);

        // When
        user.debitBalance(debitAmount);

        // Then
        assertThat(user.getBalance()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("Should throw exception when debiting with insufficient funds")
    void shouldThrowExceptionWhenDebitingWithInsufficientFunds() {
        // Given
        User user = User.create(validName, validPhone, validPin);
        Money initialAmount = Money.of(BigDecimal.valueOf(50.00));
        Money debitAmount = Money.of(BigDecimal.valueOf(100.00));

        user.creditBalance(initialAmount);

        // When & Then
        assertThatThrownBy(() -> user.debitBalance(debitAmount))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    @DisplayName("Should check sufficient balance correctly")
    void shouldCheckSufficientBalanceCorrectly() {
        // Given
        User user = User.create(validName, validPhone, validPin);
        Money balance = Money.of(BigDecimal.valueOf(100.00));
        Money smallerAmount = Money.of(BigDecimal.valueOf(50.00));
        Money equalAmount = Money.of(BigDecimal.valueOf(100.00));
        Money largerAmount = Money.of(BigDecimal.valueOf(150.00));

        user.creditBalance(balance);

        // When & Then
        assertThat(user.hasSufficientBalance(smallerAmount)).isTrue();
        assertThat(user.hasSufficientBalance(equalAmount)).isTrue();
        assertThat(user.hasSufficientBalance(largerAmount)).isFalse();
    }

    @Test
    @DisplayName("Should check insufficient balance for zero balance")
    void shouldCheckInsufficientBalanceForZeroBalance() {
        // Given
        User user = User.create(validName, validPhone, validPin);
        Money anyAmount = Money.of(BigDecimal.valueOf(1.00));

        // When & Then
        assertThat(user.hasSufficientBalance(anyAmount)).isFalse();
        assertThat(user.hasSufficientBalance(Money.zero())).isTrue();
    }

    @Test
    @DisplayName("Should handle multiple credit operations")
    void shouldHandleMultipleCreditOperations() {
        // Given
        User user = User.create(validName, validPhone, validPin);
        Money amount1 = Money.of(BigDecimal.valueOf(50.00));
        Money amount2 = Money.of(BigDecimal.valueOf(30.00));
        Money expectedTotal = Money.of(BigDecimal.valueOf(80.00));

        // When
        user.creditBalance(amount1);
        user.creditBalance(amount2);

        // Then
        assertThat(user.getBalance()).isEqualTo(expectedTotal);
    }

    @Test
    @DisplayName("Should handle multiple debit operations")
    void shouldHandleMultipleDebitOperations() {
        // Given
        User user = User.create(validName, validPhone, validPin);
        Money initialAmount = Money.of(BigDecimal.valueOf(100.00));
        Money debit1 = Money.of(BigDecimal.valueOf(30.00));
        Money debit2 = Money.of(BigDecimal.valueOf(20.00));
        Money expectedBalance = Money.of(BigDecimal.valueOf(50.00));

        user.creditBalance(initialAmount);

        // When
        user.debitBalance(debit1);
        user.debitBalance(debit2);

        // Then
        assertThat(user.getBalance()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("Should reconstitute User from existing data")
    void shouldReconstituteUserFromExistingData() {
        // Given
        UserId userId = UserId.generate();
        Money balance = Money.of(BigDecimal.valueOf(150.00));
        HashedPin hashedPin = HashedPin.fromRawPin(validPin);

        // When
        User user = User.reconstitute(userId, validName, validPhone, hashedPin, balance);

        // Then
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getName()).isEqualTo(validName);
        assertThat(user.getPhone()).isEqualTo(validPhone);
        assertThat(user.getBalance()).isEqualTo(balance);
        assertThat(user.hasValidPin(validPin)).isTrue();
    }

    @Test
    @DisplayName("Should be equal when IDs are same")
    void shouldBeEqualWhenIdsAreSame() {
        // Given
        UserId userId = UserId.generate();
        HashedPin hashedPin = HashedPin.fromRawPin(validPin);

        User user1 = User.reconstitute(userId, validName, validPhone, hashedPin, Money.zero());
        User user2 = User.reconstitute(userId, validName, validPhone, hashedPin, Money.zero());

        // When & Then
        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when IDs are different")
    void shouldNotBeEqualWhenIdsAreDifferent() {
        // Given
        User user1 = User.create(validName, validPhone, validPin);
        User user2 = User.create(validName, validPhone, validPin);

        // When & Then
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("Should update timestamp when balance changes")
    void shouldUpdateTimestampWhenBalanceChanges() {
        // Given
        User user = User.create(validName, validPhone, validPin);
        var initialUpdatedAt = user.getUpdatedAt();

        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        user.creditBalance(Money.of(BigDecimal.valueOf(100.00)));

        // Then
        assertThat(user.getUpdatedAt()).isAfter(initialUpdatedAt);
    }

    @Test
    @DisplayName("Should have meaningful toString representation")
    void shouldHaveMeaningfulToStringRepresentation() {
        // Given
        User user = User.create(validName, validPhone, validPin);

        // When
        String toString = user.toString();

        // Then
        assertThat(toString).contains("User{");
        assertThat(toString).contains("id=");
        assertThat(toString).contains("name=");
        assertThat(toString).contains("phone=");
        assertThat(toString).contains("balance=");
        // Should not expose the raw PIN (but phone number is OK)
        assertThat(toString).doesNotContain("PIN");
        assertThat(toString).doesNotContain("pin");
    }

    @Test
    @DisplayName("Should handle exact balance debit")
    void shouldHandleExactBalanceDebit() {
        // Given
        User user = User.create(validName, validPhone, validPin);
        Money amount = Money.of(BigDecimal.valueOf(100.00));

        user.creditBalance(amount);

        // When
        user.debitBalance(amount);

        // Then
        assertThat(user.getBalance()).isEqualTo(Money.zero());
    }

    @Test
    @DisplayName("Should handle zero amount credit")
    void shouldHandleZeroAmountCredit() {
        // Given
        User user = User.create(validName, validPhone, validPin);
        Money initialBalance = user.getBalance();

        // When
        user.creditBalance(Money.zero());

        // Then
        assertThat(user.getBalance()).isEqualTo(initialBalance);
    }

    @Test
    @DisplayName("Should handle zero amount debit")
    void shouldHandleZeroAmountDebit() {
        // Given
        User user = User.create(validName, validPhone, validPin);
        Money initialBalance = user.getBalance();

        // When
        user.debitBalance(Money.zero());

        // Then
        assertThat(user.getBalance()).isEqualTo(initialBalance);
    }
}