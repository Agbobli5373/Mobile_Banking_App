package com.mobilebanking.health;

import com.mobilebanking.auth.domain.JwtTokenService;
import com.mobilebanking.shared.domain.UserId;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for JWT token service functionality
 */
@Component
public class JwtHealthIndicator implements HealthIndicator {

    private final JwtTokenService jwtTokenService;

    public JwtHealthIndicator(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Health health() {
        try {
            // Test JWT token generation and validation
            UserId testUserId = UserId.fromString("test-user-id");
            String token = jwtTokenService.generateToken(testUserId);

            if (token != null && !token.isEmpty()) {
                // Try to extract username from token to verify it's valid
                String extractedUsername = jwtTokenService.extractUsername(token);

                if ("test-user-id".equals(extractedUsername)) {
                    return Health.up()
                            .withDetail("jwt", "Token generation and parsing working")
                            .withDetail("status", "JWT service operational")
                            .withDetail("extractedUsername", extractedUsername)
                            .build();
                } else {
                    return Health.down()
                            .withDetail("jwt", "Token parsing failed")
                            .withDetail("status", "JWT parsing error")
                            .withDetail("expected", "test-user-id")
                            .withDetail("actual", extractedUsername)
                            .build();
                }
            } else {
                return Health.down()
                        .withDetail("jwt", "Token generation failed")
                        .withDetail("status", "JWT generation error")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("jwt", "JWT service error")
                    .withDetail("status", "JWT service unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}