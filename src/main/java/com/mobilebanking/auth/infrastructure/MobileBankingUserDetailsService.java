package com.mobilebanking.auth.infrastructure;

import com.mobilebanking.shared.domain.PhoneNumber;
import com.mobilebanking.user.domain.User;
import com.mobilebanking.user.infrastructure.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * Loads user details from the application's user repository.
 */
@Service
public class MobileBankingUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MobileBankingUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username (phone number in our case).
     *
     * @param username the username (phone number)
     * @return UserDetails object
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            PhoneNumber phoneNumber = PhoneNumber.of(username);
            User user = userRepository.findByPhone(phoneNumber)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with phone number: " + username));

            // In a real application, we might have different roles
            // For now, all users have the same "USER" role
            return new org.springframework.security.core.userdetails.User(
                    user.getId().asString(),
                    "[PROTECTED]", // We don't expose the actual PIN hash
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        } catch (Exception e) {
            throw new UsernameNotFoundException("Invalid phone number format or user not found", e);
        }
    }
}