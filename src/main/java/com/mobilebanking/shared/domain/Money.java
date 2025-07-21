package com.mobilebanking.shared.domain;

import com.mobilebanking.shared.domain.exception.InvalidMoneyException;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Money value object that handles monetary amounts with proper precision and
 * validation.
 * Uses BigDecimal internally to avoid floating-point precision issues.
 */
@Embeddable
public final class Money {
    private static final int SCALE = 2; // Two decimal places for currency
    private final BigDecimal amount;

    // JPA requires default constructor
    public Money() {
        this.amount = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Creates a Money instance from a BigDecimal amount.
     * 
     * @param amount the monetary amount
     * @return Money instance
     * @throws InvalidMoneyException if amount is null or negative
     */
    public static Money of(BigDecimal amount) {
        if (amount == null) {
            throw InvalidMoneyException.nullAmount();
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw InvalidMoneyException.negativeAmount();
        }
        return new Money(amount);
    }

    /**
     * Creates a Money instance from a double amount.
     * 
     * @param amount the monetary amount
     * @return Money instance
     * @throws InvalidMoneyException if amount is negative
     */
    public static Money of(double amount) {
        if (amount < 0) {
            throw InvalidMoneyException.negativeAmount();
        }
        return new Money(BigDecimal.valueOf(amount));
    }

    /**
     * Creates a Money instance representing zero amount.
     * 
     * @return Money instance with zero amount
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    /**
     * Adds another Money amount to this amount.
     * 
     * @param other the Money to add
     * @return new Money instance with the sum
     * @throws InvalidMoneyException if other is null
     */
    public Money add(Money other) {
        if (other == null) {
            throw InvalidMoneyException.nullMoneyOperation("add");
        }
        return new Money(this.amount.add(other.amount));
    }

    /**
     * Subtracts another Money amount from this amount.
     * 
     * @param other the Money to subtract
     * @return new Money instance with the difference
     * @throws InvalidMoneyException if other is null or result would be negative
     */
    public Money subtract(Money other) {
        if (other == null) {
            throw InvalidMoneyException.nullMoneyOperation("subtract");
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw InvalidMoneyException.negativeResult();
        }
        return new Money(result);
    }

    /**
     * Checks if this Money amount is greater than another.
     * 
     * @param other the Money to compare with
     * @return true if this amount is greater than other
     * @throws InvalidMoneyException if other is null
     */
    public boolean isGreaterThan(Money other) {
        if (other == null) {
            throw InvalidMoneyException.nullMoneyOperation("compare");
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Checks if this Money amount is greater than or equal to another.
     * 
     * @param other the Money to compare with
     * @return true if this amount is greater than or equal to other
     * @throws InvalidMoneyException if other is null
     */
    public boolean isGreaterThanOrEqual(Money other) {
        if (other == null) {
            throw InvalidMoneyException.nullMoneyOperation("compare");
        }
        return this.amount.compareTo(other.amount) >= 0;
    }

    /**
     * Checks if this Money amount is zero.
     * 
     * @return true if amount is zero
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Gets the BigDecimal representation of this Money amount.
     * 
     * @return BigDecimal amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Money money = (Money) obj;
        return Objects.equals(amount, money.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return amount.toString();
    }
}