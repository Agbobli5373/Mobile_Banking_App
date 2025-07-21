package com.mobilebanking.auth.infrastructure;

import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.shared.domain.UserId;
import com.mobilebanking.user.domain.HashedPin;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.domain.UserName;
import com.mobilebanking.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileBankingUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private MobileBankingUserDetailsService userDetailsService;
    private User testUser;
    private final String phoneNumber = "1234567890";

    @BeforeEach
    void setUp() {
        userDetailsService = new MobileBankingUserDetailsService(userRepository);

        // Create a test user
        UserId userId = UserId.generate();
        UserName userName = UserName.of("Test User");
        PhoneNumber phone = PhoneNumber.of(phoneNumber);
        HashedPin pin = HashedPin.fromRawPin("1234");

        testUser = User.reconstitute(userId, userName, phone, pin, null);
    }

    @Test
    void loadUserByUsername_withExistingUser_shouldReturnUserDetails() {
        // Given
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);

        // Then
        assertNotNull(userDetails);
        assertEquals(testUser.getId().asString(), userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_withNonExistingUser_shouldThrowException() {
        // Given
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(phoneNumber));
    }

    @Test
    void loadUserByUsername_withInvalidPhoneNumber_shouldThrowException() {
        // Given
        String invalidPhone = "invalid";

        // When/Then
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(invalidPhone));
    }
}