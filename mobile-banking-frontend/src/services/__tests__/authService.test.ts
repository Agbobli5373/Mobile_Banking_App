import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { authService } from '../authService';
import apiClient from '../apiClient';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../../types/api';

// Mock the API client
vi.mock('../apiClient', () => ({
    default: {
        post: vi.fn(),
    },
}));

describe('AuthService', () => {
    const mockUser = {
        id: '123',
        name: 'John Doe',
        phoneNumber: '1234567890',
        balance: 1000,
        createdAt: '2023-01-01T00:00:00Z',
    };

    const mockAuthResponse: AuthResponse = {
        token: 'mock-jwt-token',
        user: mockUser,
        expiresAt: '2024-01-01T00:00:00Z',
    };

    beforeEach(() => {
        // Clear localStorage before each test
        localStorage.clear();
        vi.clearAllMocks();
    });

    afterEach(() => {
        localStorage.clear();
    });

    describe('login', () => {
        it('should login successfully and store token', async () => {
            const credentials: LoginRequest = {
                phoneNumber: '1234567890',
                pin: '1234',
            };

            const mockResponse = {
                data: {
                    data: mockAuthResponse,
                },
            };

            (apiClient.post as any).mockResolvedValue(mockResponse);

            const result = await authService.login(credentials);

            expect(apiClient.post).toHaveBeenCalledWith('/auth/login', credentials);
            expect(result).toEqual(mockAuthResponse);
            expect(localStorage.getItem('auth_token')).toBe('mock-jwt-token');
            expect(localStorage.getItem('user')).toBe(JSON.stringify(mockUser));
        });

        it('should handle login failure', async () => {
            const credentials: LoginRequest = {
                phoneNumber: '1234567890',
                pin: '1234',
            };

            (apiClient.post as any).mockRejectedValue(new Error('Invalid credentials'));

            await expect(authService.login(credentials)).rejects.toThrow('Invalid credentials');
            expect(localStorage.getItem('auth_token')).toBeNull();
            expect(localStorage.getItem('user')).toBeNull();
        });
    });

    describe('register', () => {
        it('should register successfully and store token', async () => {
            const userData: RegisterRequest = {
                name: 'John Doe',
                phoneNumber: '1234567890',
                pin: '1234',
            };

            const mockResponse = {
                data: {
                    data: mockAuthResponse,
                },
            };

            (apiClient.post as any).mockResolvedValue(mockResponse);

            const result = await authService.register(userData);

            expect(apiClient.post).toHaveBeenCalledWith('/auth/register', userData);
            expect(result).toEqual(mockAuthResponse);
            expect(localStorage.getItem('auth_token')).toBe('mock-jwt-token');
            expect(localStorage.getItem('user')).toBe(JSON.stringify(mockUser));
        });
    });

    describe('refreshToken', () => {
        it('should refresh token successfully', async () => {
            const newAuthResponse = {
                ...mockAuthResponse,
                token: 'new-jwt-token',
            };

            const mockResponse = {
                data: {
                    data: newAuthResponse,
                },
            };

            (apiClient.post as any).mockResolvedValue(mockResponse);

            const result = await authService.refreshToken();

            expect(apiClient.post).toHaveBeenCalledWith('/auth/refresh');
            expect(result).toEqual(newAuthResponse);
            expect(localStorage.getItem('auth_token')).toBe('new-jwt-token');
        });
    });

    describe('logout', () => {
        it('should logout successfully and clear storage', async () => {
            localStorage.setItem('auth_token', 'mock-token');
            localStorage.setItem('user', JSON.stringify(mockUser));

            (apiClient.post as any).mockResolvedValue({});

            await authService.logout();

            expect(apiClient.post).toHaveBeenCalledWith('/auth/logout');
            expect(localStorage.getItem('auth_token')).toBeNull();
            expect(localStorage.getItem('user')).toBeNull();
        });

        it('should clear storage even if API call fails', async () => {
            localStorage.setItem('auth_token', 'mock-token');
            localStorage.setItem('user', JSON.stringify(mockUser));

            (apiClient.post as any).mockRejectedValue(new Error('Network error'));

            await authService.logout();

            expect(localStorage.getItem('auth_token')).toBeNull();
            expect(localStorage.getItem('user')).toBeNull();
        });
    });

    describe('getCurrentUser', () => {
        it('should return current user from localStorage', () => {
            localStorage.setItem('user', JSON.stringify(mockUser));

            const result = authService.getCurrentUser();

            expect(result).toEqual(mockUser);
        });

        it('should return null if no user in localStorage', () => {
            const result = authService.getCurrentUser();

            expect(result).toBeNull();
        });

        it('should return null if user data is corrupted', () => {
            localStorage.setItem('user', 'invalid-json');

            const result = authService.getCurrentUser();

            expect(result).toBeNull();
        });
    });

    describe('getToken', () => {
        it('should return token from localStorage', () => {
            localStorage.setItem('auth_token', 'mock-token');

            const result = authService.getToken();

            expect(result).toBe('mock-token');
        });

        it('should return null if no token', () => {
            const result = authService.getToken();

            expect(result).toBeNull();
        });
    });

    describe('isAuthenticated', () => {
        it('should return true when token and user exist', () => {
            localStorage.setItem('auth_token', 'mock-token');
            localStorage.setItem('user', JSON.stringify(mockUser));

            const result = authService.isAuthenticated();

            expect(result).toBe(true);
        });

        it('should return false when token is missing', () => {
            localStorage.setItem('user', JSON.stringify(mockUser));

            const result = authService.isAuthenticated();

            expect(result).toBe(false);
        });

        it('should return false when user is missing', () => {
            localStorage.setItem('auth_token', 'mock-token');

            const result = authService.isAuthenticated();

            expect(result).toBe(false);
        });
    });

    describe('validateCredentials', () => {
        it('should validate correct credentials', () => {
            const credentials: LoginRequest = {
                phoneNumber: '1234567890',
                pin: '1234',
            };

            const result = authService.validateCredentials(credentials);

            expect(result.isValid).toBe(true);
            expect(result.errors).toHaveLength(0);
        });

        it('should reject invalid phone number', () => {
            const credentials: LoginRequest = {
                phoneNumber: '123',
                pin: '1234',
            };

            const result = authService.validateCredentials(credentials);

            expect(result.isValid).toBe(false);
            expect(result.errors).toContain('Phone number must be at least 10 digits');
        });

        it('should reject invalid PIN', () => {
            const credentials: LoginRequest = {
                phoneNumber: '1234567890',
                pin: '123',
            };

            const result = authService.validateCredentials(credentials);

            expect(result.isValid).toBe(false);
            expect(result.errors).toContain('PIN must be exactly 4 digits');
        });
    });

    describe('validateRegistration', () => {
        it('should validate correct registration data', () => {
            const userData: RegisterRequest = {
                name: 'John Doe',
                phoneNumber: '1234567890',
                pin: '1234',
            };

            const result = authService.validateRegistration(userData);

            expect(result.isValid).toBe(true);
            expect(result.errors).toHaveLength(0);
        });

        it('should reject empty name', () => {
            const userData: RegisterRequest = {
                name: '',
                phoneNumber: '1234567890',
                pin: '1234',
            };

            const result = authService.validateRegistration(userData);

            expect(result.isValid).toBe(false);
            expect(result.errors).toContain('Name is required');
        });

        it('should reject short name', () => {
            const userData: RegisterRequest = {
                name: 'A',
                phoneNumber: '1234567890',
                pin: '1234',
            };

            const result = authService.validateRegistration(userData);

            expect(result.isValid).toBe(false);
            expect(result.errors).toContain('Name must be at least 2 characters');
        });
    });
});