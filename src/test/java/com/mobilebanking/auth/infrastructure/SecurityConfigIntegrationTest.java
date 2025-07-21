package com.mobilebanking.auth.infrastructure;

import com.mobilebanking.auth.domain.JwtTokenService;
import com.mobilebanking.shared.domain.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void publicEndpoints_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isNotFound()); // Not 401, because it's permitted but endpoint doesn't exist
    }

    @Test
    void protectedEndpoints_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/wallet/balance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void protectedEndpoints_withMockAuthentication_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/wallet/balance"))
                .andExpect(status().isNotFound()); // Not 401, because it's authenticated but endpoint doesn't exist
    }

    @Test
    void protectedEndpoints_withValidJwt_shouldBeAccessible() throws Exception {
        // Given
        UserId userId = UserId.generate();
        String token = jwtTokenService.generateToken(userId);

        // Mock the UserDetailsService to return a valid user
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername(userId.asString())
                        .password("password")
                        .authorities("ROLE_USER")
                        .build());

        // When/Then
        mockMvc.perform(get("/api/wallet/balance")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound()); // Not 401, because it's authenticated but endpoint doesn't exist
    }

    @Test
    void protectedEndpoints_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/wallet/balance")
                .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isUnauthorized());
    }
}