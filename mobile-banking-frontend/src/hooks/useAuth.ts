import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { tokenStorage, jwtUtils, type StoredUser } from '../utils/tokenStorage';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types/api';

export interface AuthState {
    user: StoredUser | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    error: string | null;
}

export interface AuthActions {
    login: (credentials: LoginRequest) => Promise<void>;
    register: (userData: RegisterRequest) => Promise<void>;
    logout: () => Promise<void>;
    refreshToken: () => Promise<void>;
    clearError: () => void;
    checkAuthStatus: () => void;
}

export type UseAuthReturn = AuthState & AuthActions;

/**
 * Custom hook for authentication state management
 */
export const useAuth = (): UseAuthReturn => {
    const navigate = useNavigate();

    const [state, setState] = useState<AuthState>({
        user: null,
        isAuthenticated: false,
        isLoading: true,
        error: null,
    });

    /**
     * Update authentication state
     */
    const updateAuthState = useCallback((updates: Partial<AuthState>) => {
        setState(prev => ({ ...prev, ...updates }));
    }, []);

    /**
     * Set error state
     */
    const setError = useCallback((error: string | null) => {
        updateAuthState({ error, isLoading: false });
    }, [updateAuthState]);

    /**
     * Clear error state
     */
    const clearError = useCallback(() => {
        updateAuthState({ error: null });
    }, [updateAuthState]);

    /**
     * Set authenticated state
     */
    const setAuthenticated = useCallback((user: StoredUser) => {
        updateAuthState({
            user,
            isAuthenticated: true,
            isLoading: false,
            error: null,
        });
    }, [updateAuthState]);

    /**
     * Set unauthenticated state
     */
    const setUnauthenticated = useCallback(() => {
        updateAuthState({
            user: null,
            isAuthenticated: false,
            isLoading: false,
            error: null,
        });
    }, [updateAuthState]);

    /**
     * Check current authentication status
     */
    const checkAuthStatus = useCallback(() => {
        try {
            const token = tokenStorage.getToken();
            const user = tokenStorage.getUser();

            if (!token || !user) {
                setUnauthenticated();
                return;
            }

            // Check if token is expired
            if (jwtUtils.isTokenExpired(token)) {
                // Try to refresh token if it's expired
                refreshToken().catch(() => {
                    setUnauthenticated();
                    tokenStorage.clearAuthData();
                });
                return;
            }

            setAuthenticated(user);
        } catch (error) {
            console.error('Error checking auth status:', error);
            setUnauthenticated();
            tokenStorage.clearAuthData();
        }
    }, []);

    /**
     * Login user
     */
    const login = useCallback(async (credentials: LoginRequest): Promise<void> => {
        try {
            updateAuthState({ isLoading: true, error: null });

            // Validate credentials format
            const validation = authService.validateCredentials(credentials);
            if (!validation.isValid) {
                throw new Error(validation.errors.join(', '));
            }

            const authResponse: AuthResponse = await authService.login(credentials);

            // Store authentication data
            tokenStorage.setAuthData(
                authResponse.token,
                authResponse.user,
                // Note: Backend doesn't provide refresh token yet, but we're prepared for it
            );

            setAuthenticated(authResponse.user);

            // Navigate to dashboard after successful login
            navigate('/dashboard', { replace: true });
        } catch (error: any) {
            const errorMessage = error.response?.data?.message || error.message || 'Login failed';
            setError(errorMessage);
            throw error;
        }
    }, [navigate, updateAuthState, setError, setAuthenticated]);

    /**
     * Register new user
     */
    const register = useCallback(async (userData: RegisterRequest): Promise<void> => {
        try {
            updateAuthState({ isLoading: true, error: null });

            // Validate registration data format
            const validation = authService.validateRegistration(userData);
            if (!validation.isValid) {
                throw new Error(validation.errors.join(', '));
            }

            const authResponse: AuthResponse = await authService.register(userData);

            // Store authentication data
            tokenStorage.setAuthData(
                authResponse.token,
                authResponse.user,
            );

            setAuthenticated(authResponse.user);

            // Navigate to dashboard after successful registration
            navigate('/dashboard', { replace: true });
        } catch (error: any) {
            const errorMessage = error.response?.data?.message || error.message || 'Registration failed';
            setError(errorMessage);
            throw error;
        }
    }, [navigate, updateAuthState, setError, setAuthenticated]);

    /**
     * Logout user
     */
    const logout = useCallback(async (): Promise<void> => {
        try {
            updateAuthState({ isLoading: true });

            // Call logout service (which handles API call and cleanup)
            await authService.logout();

            setUnauthenticated();

            // Navigate to login page
            navigate('/login', { replace: true });
        } catch (error: any) {
            console.error('Logout error:', error);
            // Even if logout API fails, clear local state
            tokenStorage.clearAuthData();
            setUnauthenticated();
            navigate('/login', { replace: true });
        }
    }, [navigate, updateAuthState, setUnauthenticated]);

    /**
     * Refresh authentication token
     */
    const refreshToken = useCallback(async (): Promise<void> => {
        try {
            const authResponse: AuthResponse = await authService.refreshToken();

            // Update stored authentication data
            tokenStorage.setAuthData(
                authResponse.token,
                authResponse.user,
            );

            setAuthenticated(authResponse.user);
        } catch (error: any) {
            console.error('Token refresh failed:', error);
            // If refresh fails, logout user
            tokenStorage.clearAuthData();
            setUnauthenticated();
            navigate('/login', { replace: true });
            throw error;
        }
    }, [navigate, setAuthenticated, setUnauthenticated]);

    /**
     * Set up automatic token refresh
     */
    useEffect(() => {
        let refreshInterval: NodeJS.Timeout;

        if (state.isAuthenticated) {
            // Check token expiration every minute
            refreshInterval = setInterval(() => {
                const token = tokenStorage.getToken();
                if (token && jwtUtils.willTokenExpireSoon(token, 5)) {
                    refreshToken().catch(console.error);
                }
            }, 60000); // Check every minute
        }

        return () => {
            if (refreshInterval) {
                clearInterval(refreshInterval);
            }
        };
    }, [state.isAuthenticated, refreshToken]);

    /**
     * Initialize authentication state on mount
     */
    useEffect(() => {
        checkAuthStatus();
    }, [checkAuthStatus]);

    return {
        // State
        user: state.user,
        isAuthenticated: state.isAuthenticated,
        isLoading: state.isLoading,
        error: state.error,

        // Actions
        login,
        register,
        logout,
        refreshToken,
        clearError,
        checkAuthStatus,
    };
};