package com.mobilebanking.user.domain;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.shared.domain.exception.InsufficientFundsException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User aggregate root that represents a banking customer.
 * Contains core user information and wallet functionality.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_phone", columnList = "phone")
})
public class User {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id; // Store UUID as string for JPA

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    private UserName name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone", nullable = false, unique = true))
    private PhoneNumber phone;

    @Embedded
    @AttributeOverride(name = "hashedValue", column = @Column(name = "pin_hash", nullable = false))
    private HashedPin pin;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "balance", precision = 19, scale = 2, nullable = false))
    private Money balance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA requires default constructor
    protected User() {
    }

    private User(UserId id, UserName name, PhoneNumber phone, HashedPin pin) {
        this.id = id.asString();
        this.name = name;
        this.phone = phone;
        this.pin = pin;
        this.balance = Money.zero();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Creates a new User with the provided details.
     * 
     * @param name   the user's name
     * @param phone  the user's phone number
     * @param rawPin the user's raw PIN
     * @return new User instance
     */
    public static User create(UserName name, PhoneNumber phone, String rawPin) {
        UserId userId = UserId.generate();
        HashedPin hashedPin = HashedPin.fromRawPin(rawPin);
        return new User(userId, name, phone, hashedPin);
    }

    /**
     * Creates a User from existing data (for repository loading).
     * 
     * @param id        the user ID
     * @param name      the user's name
     * @param phone     the user's phone number
     * @param hashedPin the user's hashed PIN
     * @param balance   the user's current balance
     * @return User instance
     */
    public static User reconstitute(UserId id, UserName name, PhoneNumber phone,
            HashedPin hashedPin, Money balance) {
        User user = new User(id, name, phone, hashedPin);
        user.balance = balance;
        return user;
    }

    /**
     * Debits the specified amount from the user's balance.
     * 
     * @param amount the amount to debit
     * @throws InsufficientFundsException if balance is insufficient
     */
    public void debitBalance(Money amount) {
        if (!hasSufficientBalance(amount)) {
            throw InsufficientFundsException.forAmount(amount, this.balance);
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Credits the specified amount to the user's balance.
     * 
     * @param amount the amount to credit
     */
    public void creditBalance(Money amount) {
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validates if the provided raw PIN matches the user's PIN.
     * 
     * @param rawPin the raw PIN to validate
     * @return true if PIN is valid, false otherwise
     */
    public boolean hasValidPin(String rawPin) {
        return this.pin.matches(rawPin);
    }

    /**
     * Checks if the user has sufficient balance for the specified amount.
     * 
     * @param amount the amount to check
     * @return true if balance is sufficient, false otherwise
     */
    public boolean hasSufficientBalance(Money amount) {
        return this.balance.isGreaterThanOrEqual(amount);
    }

    // Getters
    public UserId getId() {
        return UserId.fromString(this.id);
    }

    public UserName getName() {
        return name;
    }

    public PhoneNumber getPhone() {
        return phone;
    }

    public Money getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        User user = (User) obj;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("User{id='%s', name='%s', phone='%s', balance=%s}",
                id, name, phone, balance);
    }
}