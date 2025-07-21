package com.mobilebanking.auth.domain;

import com.mobilebanking.shared.domain.UserId;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;
    private final String testSecret = "testSecretKeyWithAtLeast256BitsForHmacSha256Algorithm";
    private final long validityHours = 24;
    private final String testUserId = "123e4567-e89b-12d3-a456-426614174000";
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(testSecret, validityHours);
        userDetails = new User(testUserId, "password", Collections.emptyList());
    }

    @Test
    void generateToken_withUserId_shouldCreateValidToken() {
        // Given
        UserId userId = UserId.fromString(testUserId);
        
        // When
        String token = jwtTokenService.generateToken(userId);
        
        // Then
        assertNotNull(token);
        assertEquals(testUserId, jwtTokenService.extractUsername(token));
    }

    @Test
    void generateToken_withUserDetails_shouldCreateValidToken() {
        // When
        String token = jwtTokenService.generateToken(userDetails);
        
        // Then
        assertNotNull(token);
        assertEquals(testUserId, jwtTokenService.extractUsername(token));
    }

    @Test
    void generateToken_withExtraClaims_shouldIncludeClaimsInToken() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("testKey", "testValue");
        
        // When
        String token = jwtTokenService.generateToken(extraClaims, testUserId);
        
        // Then
        assertNotNull(token);
        assertEquals("testValue", jwtTokenService.extractClaim(token, claims -> claims.get("testKey")));
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        // Given
        String token = jwtTokenService.generateToken(userDetails);
        
        // When
        boolean isValid = jwtTokenService.validateToken(token, userDetails);
        
        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_withInvalidUsername_shouldReturnFalse() {
        // Given
        String token = jwtTokenService.generateToken(userDetails);
        UserDetails differentUser = new User("different-user", "password", Collections.emptyList());
        
        // When
        boolean isValid = jwtTokenService.validateToken(token, differentUser);
        
        // Then
        assertFalse(isValid);
    }

    @Test
    void extractUsername_withValidToken_shouldReturnUsername() {
        // Given
        String token = jwtTokenService.generateToken(userDetails);
        
        // When
        String username = jwtTokenService.extractUsername(token);
        
        // Then
        assertEquals(testUserId, username);
    }

    @Test
    void extractClaim_withValidToken_shouldReturnClaimValue() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");
        String token = jwtTokenService.generateToken(extraClaims, testUserId);
        
        // When
        String role = jwtTokenService.extractClaim(token, claims -> claims.get("role", String.class));
        
        // Then
        assertEquals("USER", role);
    }
}