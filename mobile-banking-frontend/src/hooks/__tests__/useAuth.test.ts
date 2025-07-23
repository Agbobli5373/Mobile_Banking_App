import { renderHook, act, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { useAuth } from '../useAuth';
import { authService } from '../../services/authService';
import { tokenStorage } from '../../utils/tokenStorage';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../../types/api';

// Mock dependencies
vi.mock('../../services/authService');
vi.mock('../../utils/tokenStorage');
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => vi.fn(),
    };
});

const mockAuthService = vi.mocked(authService);
const mockTokenStorage = vi.mocked(tokenStorage);

// Test data
const mockUser = {
    id: '1',
    name: 'John Doe',
    phone: '1234567890',
    balance: 1000,
    createdAt: '2024-01-01T00:00:00Z',
};

const mockAuthResponse: AuthResponse = {
    token: 'mock-jwt-token',
    user: mockUser,
    expiresAt: '2024-12-31T23:59:59Z',
};

const mockLoginRequest: LoginRequest = {
    phone: '1234567890',
    pin: '1234',
};

const mockRegisterRequest: RegisterRequest = {
    name: 'John Doe',
    phone: '1234567890',
    pin: '1234',
};

// Wrapper component for React Router
const wrapper = ({ children }: { children: React.ReactNode }) => (
    <BrowserRouter>{ children } </BrowserRouter>
);

describe('useAuth', () => {
    beforeEach(() => {
        vi.clearAllMocks();

        // Default mock implementations
        mockTokenStorage.getToken.mockReturnValue(null);
        mockTokenStorage.getUser.mockReturnValue(null);
        mockTokenStorage.hasCompleteAuthData.mockReturnValue(false);

        mockAuthService.validateCredentials.mockReturnValue({
            isValid: true,
            errors: [],
        });

        mockAuthService.validateRegistration.mockReturnValue({
            isValid: true,
            errors: [],
        });
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    describe('initialization', () => {
        it('should initialize with unauthenticated state when no token exists', async () => {
            const { result } = renderHook(() => useAuth(), { wrapper });

            await waitFor(() => {
                expect(result.current.isLoading).toBe(false);
            });

            expect(result.current.isAuthenticated).toBe(false);
            expect(result.current.user).toBeNull();
            expect(result.current.error).toBeNull();
        });

        it('should initialize with authenticated state when valid token exists', async () => {
            mockTokenStorage.getToken.mockReturnValue('valid-token');
            mockTokenStorage.getUser.mockReturnValue(mockUser);

            // Mock JWT utils to return non-expired token
            vi.doMock('../../utils/tokenStorage', () => ({
                ...mockTokenStorage,
                jwtUtils: {
                    isTokenExpired: vi.fn().mockReturnValue(false),
                },
            }));

            const { result } = renderHook(() => useAuth(), { wrapper });

            await waitFor(() => {
                expect(result.current.isLoading).toBe(false);
            });

            expect(result.current.isAuthenticated).toBe(true);
            expect(result.current.user).toEqual(mockUser);
        });
    });

    describe('login', () => {
        it('should successfully login user with valid credentials', async () => {
            mockAuthService.login.mockResolvedValue(mockAuthResponse);

            const { result } = renderHook(() => useAuth(), { wrapper });

            await act(async () => {
                await result.current.login(mockLoginRequest);
            });

            expect(mockAuthService.login).toHaveBeenCalledWith(mockLoginRequest);
            expect(mockTokenStorage.setAuthData).toHaveBeenCalledWith(
                mockAuthResponse.token,
                mockAuthResponse.user,
                undefined
            );
            expect(result.current.isAuthenticated).toBe(true);
            expect(result.current.user).toEqual(mockUser);
            expect(result.current.error).toBeNull();
        });

        it('should handle login validation errors', async () => {
            mockAuthService.validateCredentials.mockReturnValue({
                isValid: false,
                errors: ['Phone number is required', 'PIN must be exactly 4 digits'],
            });

            const { result } = renderHook(() => useAuth(), { wrapper });

            await act(async () => {
                try {
                    await result.current.login(mockLoginRequest);
                } catch (error) {
                    // Expected to throw
                }
            });

            expect(result.current.error).toBe('Phone number is required, PIN must be exactly 4 digits');
            expect(result.current.isAuthenticated).toBe(false);
        });

        it('should handle login API errors', async () => {
            const apiError = {
                response: {
                    data: {
                        message: 'Invalid credentials',
                    },
                },
            };
            mockAuthService.login.mockRejectedValue(apiError);

            const { result } = renderHook(() => useAuth(), { wrapper });

            await act(async () => {
                try {
                    await result.current.login(mockLoginRequest);
                } catch (error) {
                    // Expected to throw
                }
            });

            expect(result.current.error).toBe('Invalid credentials');
            expect(result.current.isAuthenticated).toBe(false);
        });

        it('should set loading state during login', async () => {
            let resolveLogin: (value: AuthResponse) => void;
            const loginPromise = new Promise<AuthResponse>((resolve) => {
                resolveLogin = resolve;
            });
            mockAuthService.login.mockReturnValue(loginPromise);

            const { result } = renderHook(() => useAuth(), { wrapper });

            act(() => {
                result.current.login(mockLoginRequest);
            });

            expect(result.current.isLoading).toBe(true);

            await act(async () => {
                resolveLogin!(mockAuthResponse);
                await loginPromise;
            });

            expect(result.current.isLoading).toBe(false);
        });
    });

    describe('register', () => {
        it('should successfully register user with valid data', async () => {
            mockAuthService.register.mockResolvedValue(mockAuthResponse);

            const { result } = renderHook(() => useAuth(), { wrapper });

            await act(async () => {
                await result.current.register(mockRegisterRequest);
            });

            expect(mockAuthService.register).toHaveBeenCalledWith(mockRegisterRequest);
            expect(mockTokenStorage.setAuthData).toHaveBeenCalledWith(
                mockAuthResponse.token,
                mockAuthResponse.user
            );
            expect(result.current.isAuthenticated).toBe(true);
            expect(result.current.user).toEqual(mockUser);
        });

        it('should handle registration validation errors', async () => {
            mockAuthService.validateRegistration.mockReturnValue({
                isValid: false,
                errors: ['Name is required'],
            });

            const { result } = renderHook(() => useAuth(), { wrapper });

            await act(async () => {
                try {
                    await result.current.register(mockRegisterRequest);
                } catch (error) {
                    // Expected to throw
                }
            });

            expect(result.current.error).toBe('Name is required');
            expect(result.current.isAuthenticated).toBe(false);
        });

        it('should handle registration API errors', async () => {
            const apiError = {
                response: {
                    data: {
                        message: 'Phone number already exists',
                    },
                },
            };
            mockAuthService.register.mockRejectedValue(apiError);

            const { result } = renderHook(() => useAuth(), { wrapper });

            await act(async () => {
                try {
                    await result.current.register(mockRegisterRequest);
                } catch (error) {
                    // Expected to throw
                }
            });

            expect(result.current.error).toBe('Phone number already exists');
            expect(result.current.isAuthenticated).toBe(false);
        });
    });

    describe('logout', () => {
        it('should successfully logout user', async () => {
            // Start with authenticated state
            mockTokenStorage.getToken.mockReturnValue('valid-token');
            mockTokenStorage.getUser.mockReturnValue(mockUser);
            mockAuthService.logout.mockResolvedValue();

            const { result } = renderHook(() => useAuth(), { wrapper });

            // Wait for initial auth check
            await waitFor(() => {
                expect(result.current.isAuthenticated).toBe(true);
            });

            await act(async () => {
                await result.current.logout();
            });

            expect(mockAuthService.logout).toHaveBeenCalled();
            expect(result.current.isAuthenticated).toBe(false);
            expect(result.current.user).toBeNull();
        });

        it('should logout even if API call fails', async () => {
            mockTokenStorage.getToken.mockReturnValue('valid-token');
            mockTokenStorage.getUser.mockReturnValue(mockUser);
            mockAuthService.logout.mockRejectedValue(new Error('Network error'));

            const { result } = renderHook(() => useAuth(), { wrapper });

            await act(async () => {
                await result.current.logout();
            });

            expect(mockTokenStorage.clearAuthData).toHaveBeenCalled();
            expect(result.current.isAuthenticated).toBe(false);
            expect(result.current.user).toBeNull();
        });
    });

    describe('token refresh', () => {
        it('should successfully refresh token', async () => {
            const newAuthResponse = {
                ...mockAuthResponse,
                token: 'new-token',
            };
            mockAuthService.refreshToken.mockResolvedValue(newAuthResponse);

            const { result } = renderHook(() => useAuth(), { wrapper });

            await act(async () => {
                await result.current.refreshToken();
            });

            expect(mockAuthService.refreshToken).toHaveBeenCalled();
            expect(mockTokenStorage.setAuthData).toHaveBeenCalledWith(
                newAuthResponse.token,
                newAuthResponse.user
            );
        });

        it('should logout user if token refresh fails', async () => {
            mockAuthService.refreshToken.mockRejectedValue(new Error('Refresh failed'));

            const { result } = renderHook(() => useAuth(), { wrapper });

            await act(async () => {
                try {
                    await result.current.refreshToken();
                } catch (error) {
                    // Expected to throw
                }
            });

            expect(mockTokenStorage.clearAuthData).toHaveBeenCalled();
            expect(result.current.isAuthenticated).toBe(false);
        });
    });

    describe('error handling', () => {
        it('should clear error state', async () => {
            const { result } = renderHook(() => useAuth(), { wrapper });

            // Set error state
            await act(async () => {
                try {
                    await result.current.login({
                        phone: '',
                        pin: '',
                    });
                } catch (error) {
                    // Expected to throw
                }
            });

            expect(result.current.error).toBeTruthy();

            // Clear error
            act(() => {
                result.current.clearError();
            });

            expect(result.current.error).toBeNull();
        });
    });

    describe('auth status check', () => {
        it('should check auth status and update state', async () => {
            mockTokenStorage.getToken.mockReturnValue('valid-token');
            mockTokenStorage.getUser.mockReturnValue(mockUser);

            const { result } = renderHook(() => useAuth(), { wrapper });

            act(() => {
                result.current.checkAuthStatus();
            });

            await waitFor(() => {
                expect(result.current.isAuthenticated).toBe(true);
            });

            expect(result.current.user).toEqual(mockUser);
        });

        it('should handle missing token or user data', async () => {
            mockTokenStorage.getToken.mockReturnValue(null);
            mockTokenStorage.getUser.mockReturnValue(null);

            const { result } = renderHook(() => useAuth(), { wrapper });

            act(() => {
                result.current.checkAuthStatus();
            });

            await waitFor(() => {
                expect(result.current.isAuthenticated).toBe(false);
            });

            expect(result.current.user).toBeNull();
        });
    });
});