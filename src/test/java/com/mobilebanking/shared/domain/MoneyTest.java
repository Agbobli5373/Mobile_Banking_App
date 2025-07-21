package com.mobilebanking.shared.domain;

import com.mobilebanking.shared.domain.exception.InvalidMoneyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create Money from valid BigDecimal")
        void shouldCreateMoneyFromValidBigDecimal() {
            // Given
            BigDecimal amount = new BigDecimal("100.50");

            // When
            Money money = Money.of(amount);

            // Then
            assertEquals(new BigDecimal("100.50"), money.getAmount());
        }

        @Test
        @DisplayName("Should create Money from valid double")
        void shouldCreateMoneyFromValidDouble() {
            // Given
            double amount = 100.50;

            // When
            Money money = Money.of(amount);

            // Then
            assertEquals(new BigDecimal("100.50"), money.getAmount());
        }

        @Test
        @DisplayName("Should create zero Money")
        void shouldCreateZeroMoney() {
            // When
            Money money = Money.zero();

            // Then
            assertEquals(BigDecimal.ZERO.setScale(2), money.getAmount());
            assertTrue(money.isZero());
        }

        @Test
        @DisplayName("Should throw exception for null BigDecimal")
        void shouldThrowExceptionForNullBigDecimal() {
            // When & Then
            InvalidMoneyException exception = assertThrows(
                    InvalidMoneyException.class,
                    () -> Money.of((BigDecimal) null));
            assertEquals("Amount cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for negative BigDecimal")
        void shouldThrowExceptionForNegativeBigDecimal() {
            // Given
            BigDecimal negativeAmount = new BigDecimal("-10.00");

            // When & Then
            InvalidMoneyException exception = assertThrows(
                    InvalidMoneyException.class,
                    () -> Money.of(negativeAmount));
            assertEquals("Amount cannot be negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for negative double")
        void shouldThrowExceptionForNegativeDouble() {
            // Given
            double negativeAmount = -10.00;

            // When & Then
            InvalidMoneyException exception = assertThrows(
                    InvalidMoneyException.class,
                    () -> Money.of(negativeAmount));
            assertEquals("Amount cannot be negative", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Arithmetic Operations Tests")
    class ArithmeticOperationsTests {

        @Test
        @DisplayName("Should add two Money amounts correctly")
        void shouldAddTwoMoneyAmountsCorrectly() {
            // Given
            Money money1 = Money.of(100.50);
            Money money2 = Money.of(50.25);

            // When
            Money result = money1.add(money2);

            // Then
            assertEquals(Money.of(150.75), result);
        }

        @Test
        @DisplayName("Should subtract two Money amounts correctly")
        void shouldSubtractTwoMoneyAmountsCorrectly() {
            // Given
            Money money1 = Money.of(100.50);
            Money money2 = Money.of(50.25);

            // When
            Money result = money1.subtract(money2);

            // Then
            assertEquals(Money.of(50.25), result);
        }

        @Test
        @DisplayName("Should throw exception when adding null Money")
        void shouldThrowExceptionWhenAddingNullMoney() {
            // Given
            Money money = Money.of(100.00);

            // When & Then
            InvalidMoneyException exception = assertThrows(
                    InvalidMoneyException.class,
                    () -> money.add(null));
            assertEquals("Cannot add null Money", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when subtracting null Money")
        void shouldThrowExceptionWhenSubtractingNullMoney() {
            // Given
            Money money = Money.of(100.00);

            // When & Then
            InvalidMoneyException exception = assertThrows(
                    InvalidMoneyException.class,
                    () -> money.subtract(null));
            assertEquals("Cannot subtract null Money", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when subtraction results in negative amount")
        void shouldThrowExceptionWhenSubtractionResultsInNegativeAmount() {
            // Given
            Money money1 = Money.of(50.00);
            Money money2 = Money.of(100.00);

            // When & Then
            InvalidMoneyException exception = assertThrows(
                    InvalidMoneyException.class,
                    () -> money1.subtract(money2));
            assertEquals("Operation result cannot be negative", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Comparison Tests")
    class ComparisonTests {

        @Test
        @DisplayName("Should return true when first amount is greater than second")
        void shouldReturnTrueWhenFirstAmountIsGreaterThanSecond() {
            // Given
            Money money1 = Money.of(100.00);
            Money money2 = Money.of(50.00);

            // When & Then
            assertTrue(money1.isGreaterThan(money2));
            assertFalse(money2.isGreaterThan(money1));
        }

        @Test
        @DisplayName("Should return true when first amount is greater than or equal to second")
        void shouldReturnTrueWhenFirstAmountIsGreaterThanOrEqualToSecond() {
            // Given
            Money money1 = Money.of(100.00);
            Money money2 = Money.of(50.00);
            Money money3 = Money.of(100.00);

            // When & Then
            assertTrue(money1.isGreaterThanOrEqual(money2));
            assertTrue(money1.isGreaterThanOrEqual(money3));
            assertFalse(money2.isGreaterThanOrEqual(money1));
        }

        @Test
        @DisplayName("Should throw exception when comparing with null Money")
        void shouldThrowExceptionWhenComparingWithNullMoney() {
            // Given
            Money money = Money.of(100.00);

            // When & Then
            assertThrows(InvalidMoneyException.class, () -> money.isGreaterThan(null));
            assertThrows(InvalidMoneyException.class, () -> money.isGreaterThanOrEqual(null));
        }

        @Test
        @DisplayName("Should correctly identify zero amount")
        void shouldCorrectlyIdentifyZeroAmount() {
            // Given
            Money zeroMoney = Money.zero();
            Money nonZeroMoney = Money.of(100.00);

            // When & Then
            assertTrue(zeroMoney.isZero());
            assertFalse(nonZeroMoney.isZero());
        }
    }

    @Nested
    @DisplayName("Equality and Hash Tests")
    class EqualityAndHashTests {

        @Test
        @DisplayName("Should be equal when amounts are the same")
        void shouldBeEqualWhenAmountsAreTheSame() {
            // Given
            Money money1 = Money.of(100.50);
            Money money2 = Money.of(100.50);

            // When & Then
            assertEquals(money1, money2);
            assertEquals(money1.hashCode(), money2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when amounts are different")
        void shouldNotBeEqualWhenAmountsAreDifferent() {
            // Given
            Money money1 = Money.of(100.50);
            Money money2 = Money.of(200.50);

            // When & Then
            assertNotEquals(money1, money2);
        }

        @Test
        @DisplayName("Should not be equal to null or different type")
        void shouldNotBeEqualToNullOrDifferentType() {
            // Given
            Money money = Money.of(100.50);

            // When & Then
            assertNotEquals(money, null);
            assertNotEquals(money, "100.50");
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            Money money = Money.of(100.50);

            // When & Then
            assertEquals(money, money);
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should return correct string representation")
        void shouldReturnCorrectStringRepresentation() {
            // Given
            Money money = Money.of(100.50);

            // When
            String result = money.toString();

            // Then
            assertEquals("100.50", result);
        }
    }

    @Nested
    @DisplayName("Precision Tests")
    class PrecisionTests {

        @Test
        @DisplayName("Should handle precision correctly with rounding")
        void shouldHandlePrecisionCorrectlyWithRounding() {
            // Given
            BigDecimal amount = new BigDecimal("100.555");

            // When
            Money money = Money.of(amount);

            // Then
            assertEquals(new BigDecimal("100.56"), money.getAmount());
        }

        @Test
        @DisplayName("Should maintain two decimal places")
        void shouldMaintainTwoDecimalPlaces() {
            // Given
            Money money = Money.of(100);

            // When
            BigDecimal amount = money.getAmount();

            // Then
            assertEquals(2, amount.scale());
            assertEquals(new BigDecimal("100.00"), amount);
        }
    }
}