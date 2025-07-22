package com.mobilebanking.shared.infrastructure.config;

import com.mobilebanking.shared.domain.Money;
import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.transaction.domain.Transaction;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.domain.UserName;
import com.mobilebanking.user.infrastructure.repository.UserJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Database initializer for development environment.
 * Creates sample data for testing and development purposes.
 */
@Configuration
@Profile({ "dev", "test" })
public class DatabaseInitializer {

    @Bean
    @Transactional
    public CommandLineRunner initDatabase(UserJpaRepository userRepository) {
        return args -> {
            // Only initialize if the database is empty
            if (userRepository.count() == 0) {
                // Create test users
                User user1 = User.create(
                        new UserName("John Doe"),
                        new PhoneNumber("1234567890"),
                        "1234");
                user1.creditBalance(Money.of(new BigDecimal("1000.00")));

                User user2 = User.create(
                        new UserName("Jane Smith"),
                        new PhoneNumber("9876543210"),
                        "5678");
                user2.creditBalance(Money.of(new BigDecimal("500.00")));

                // Save users
                userRepository.save(user1);
                userRepository.save(user2);

                // Create a test transaction
                Transaction transaction = Transaction.createTransfer(
                        user1.getId(),
                        user2.getId(),
                        Money.of(new BigDecimal("100.00")));

                // Update balances
                user1.debitBalance(Money.of(new BigDecimal("100.00")));
                user2.creditBalance(Money.of(new BigDecimal("100.00")));

                // Save updated users
                userRepository.save(user1);
                userRepository.save(user2);

                System.out.println("Database initialized with sample data for development");
            }
        };
    }
}