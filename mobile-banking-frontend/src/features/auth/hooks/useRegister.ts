import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../../services/authService';
import { tokenStorage } from '../../../utils/tokenStorage';
import type { RegisterRequest, AuthResponse } from '../../../types/api';

export interface UseRegisterState {
    isLoading: boolean;
    error: string | null;
    isSuccess: boolean;
}

export interface UseRegisterActions {
    register: (userData: RegisterRequest) => Promise<AuthResponse>;
    clearError: () => void;
    reset: () => void;
}

export type UseRegisterReturn = UseRegisterState & UseRegisterActions;

/**
 * Custom hook for user registration
 */
export const useRegister = (): UseRegisterReturn => {
    const navigate = useNavigate();

    const [state, setState] = useState<UseRegisterState>({
        isLoading: false,
        error: null,
        isSuccess: false,
    });

    /**
     * Update state
     */
    const updateState = useCallback((updates: Partial<UseRegisterState>) => {
        setState(prev => ({ ...prev, ...updates }));
    }, []);

    /**
     * Clear error state
     */
    const clearError = useCallback(() => {
        updateState({ error: null });
    }, [updateState]);

    /**
     * Reset all state
     */
    const reset = useCallback(() => {
        setState({
            isLoading: false,
            error: null,
            isSuccess: false,
        });
    }, []);

    /**
     * Register user
     */
    const register = useCallback(async (userData: RegisterRequest): Promise<AuthResponse> => {
        try {
            updateState({ isLoading: true, error: null, isSuccess: false });

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

            updateState({
                isLoading: false,
                error: null,
                isSuccess: true
            });

            // Navigate to dashboard after successful registration
            setTimeout(() => {
                navigate('/dashboard', { replace: true });
            }, 2000); // Give user time to see success message

            return authResponse;
        } catch (error: any) {
            const errorMessage = error.response?.data?.message || error.message || 'Registration failed';
            updateState({
                isLoading: false,
                error: errorMessage,
                isSuccess: false
            });
            throw error;
        }
    }, [navigate, updateState]);

    return {
        // State
        isLoading: state.isLoading,
        error: state.error,
        isSuccess: state.isSuccess,

        // Actions
        register,
        clearError,
        reset,
    };
};