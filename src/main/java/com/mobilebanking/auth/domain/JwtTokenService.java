package com.mobilebanking.auth.domain;

import com.mobilebanking.shared.domain.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for JWT token generation and validation.
 * Handles token creation, parsing, and validation operations.
 */
@Service
public class JwtTokenService {

    private final Key secretKey;
    private final long tokenValidityMs;

    /**
     * Creates a JwtTokenService with the specified secret key and token validity.
     *
     * @param secret       the JWT secret key
     * @param validityHours the token validity period in hours
     */
    public JwtTokenService(
            @Value("${jwt.secret:defaultSecretKeyForDevelopmentEnvironmentOnly}") String secret,
            @Value("${jwt.validity.hours:24}") long validityHours) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.tokenValidityMs = validityHours * 60 * 60 * 1000;
    }

    /**
     * Generates a JWT token for the specified user ID.
     *
     * @param userId the user ID to include in the token
     * @return the generated JWT token
     */
    public String generateToken(UserId userId) {
        return generateToken(new HashMap<>(), userId.asString());
    }

    /**
     * Generates a JWT token for the specified user details.
     *
     * @param userDetails the user details
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails.getUsername());
    }

    /**
     * Generates a JWT token with additional claims.
     *
     * @param extraClaims additional claims to include in the token
     * @param subject     the subject (typically user ID)
     * @return the generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + tokenValidityMs);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a JWT token against user details.
     *
     * @param token       the JWT token to validate
     * @param userDetails the user details to validate against
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a JWT token.
     *
     * @param token          the JWT token
     * @param claimsResolver the function to extract the desired claim
     * @param <T>            the type of the claim
     * @return the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Checks if a JWT token is expired.
     *
     * @param token the JWT token
     * @return true if token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token the JWT token
     * @return the claims
     * @throws JwtException if token is invalid
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}