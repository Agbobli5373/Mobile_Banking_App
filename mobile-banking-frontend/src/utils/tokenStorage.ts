/**
 * Secure token storage utilities for authentication
 */

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'user_data';
const REFRESH_TOKEN_KEY = 'refresh_token';

export interface StoredUser {
    id: string;
    name: string;
    phoneNumber: string;
    balance: number;
    createdAt: string;
}

export interface TokenData {
    token: string;
    expiresAt: string;
    refreshToken?: string;
}

/**
 * Token storage utilities with secure localStorage handling
 */
export const tokenStorage = {
    /**
     * Get authentication token from storage
     */
    getToken(): string | null {
        try {
            return localStorage.getItem(TOKEN_KEY);
        } catch (error) {
            console.error('Error getting token from storage:', error);
            return null;
        }
    },

    /**
     * Set authentication token in storage
     */
    setToken(token: string): void {
        try {
            localStorage.setItem(TOKEN_KEY, token);
        } catch (error) {
            console.error('Error setting token in storage:', error);
        }
    },

    /**
     * Get refresh token from storage
     */
    getRefreshToken(): string | null {
        try {
            return localStorage.getItem(REFRESH_TOKEN_KEY);
        } catch (error) {
            console.error('Error getting refresh token from storage:', error);
            return null;
        }
    },

    /**
     * Set refresh token in storage
     */
    setRefreshToken(refreshToken: string): void {
        try {
            localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
        } catch (error) {
            console.error('Error setting refresh token in storage:', error);
        }
    },

    /**
     * Get user data from storage
     */
    getUser(): StoredUser | null {
        try {
            const userStr = localStorage.getItem(USER_KEY);
            return userStr ? JSON.parse(userStr) : null;
        } catch (error) {
            console.error('Error getting user from storage:', error);
            return null;
        }
    },

    /**
     * Set user data in storage
     */
    setUser(user: StoredUser): void {
        try {
            localStorage.setItem(USER_KEY, JSON.stringify(user));
        } catch (error) {
            console.error('Error setting user in storage:', error);
        }
    },

    /**
     * Store complete authentication data
     */
    setAuthData(token: string, user: StoredUser, refreshToken?: string): void {
        this.setToken(token);
        this.setUser(user);
        if (refreshToken) {
            this.setRefreshToken(refreshToken);
        }
    },

    /**
     * Clear all authentication data from storage
     */
    clearAuthData(): void {
        try {
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem(USER_KEY);
            localStorage.removeItem(REFRESH_TOKEN_KEY);
        } catch (error) {
            console.error('Error clearing auth data from storage:', error);
        }
    },

    /**
     * Check if token exists in storage
     */
    hasToken(): boolean {
        return !!this.getToken();
    },

    /**
     * Check if user data exists in storage
     */
    hasUser(): boolean {
        return !!this.getUser();
    },

    /**
     * Check if authentication data is complete
     */
    hasCompleteAuthData(): boolean {
        return this.hasToken() && this.hasUser();
    }
};

/**
 * JWT token utilities
 */
export const jwtUtils = {
    /**
     * Decode JWT token payload
     */
    decodeToken(token: string): any {
        try {
            const payload = token.split('.')[1];
            return JSON.parse(atob(payload));
        } catch (error) {
            console.error('Error decoding token:', error);
            return null;
        }
    },

    /**
     * Check if token is expired
     */
    isTokenExpired(token: string): boolean {
        try {
            const payload = this.decodeToken(token);
            if (!payload || !payload.exp) {
                return true;
            }

            const currentTime = Date.now() / 1000;
            return payload.exp < currentTime;
        } catch (error) {
            console.error('Error checking token expiration:', error);
            return true;
        }
    },

    /**
     * Get token expiration time
     */
    getTokenExpiration(token: string): Date | null {
        try {
            const payload = this.decodeToken(token);
            if (!payload || !payload.exp) {
                return null;
            }

            return new Date(payload.exp * 1000);
        } catch (error) {
            console.error('Error getting token expiration:', error);
            return null;
        }
    },

    /**
     * Check if token will expire soon (within 5 minutes)
     */
    willTokenExpireSoon(token: string, thresholdMinutes: number = 5): boolean {
        try {
            const payload = this.decodeToken(token);
            if (!payload || !payload.exp) {
                return true;
            }

            const currentTime = Date.now() / 1000;
            const thresholdTime = currentTime + (thresholdMinutes * 60);

            return payload.exp < thresholdTime;
        } catch (error) {
            console.error('Error checking token expiration threshold:', error);
            return true;
        }
    }
};