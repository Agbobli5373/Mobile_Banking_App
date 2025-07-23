import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { tokenStorage, jwtUtils, type StoredUser } from '../tokenStorage';

// Mock localStorage
const localStorageMock = {
    getItem: vi.fn(),
    setItem: vi.fn(),
    removeItem: vi.fn(),
    clear: vi.fn(),
};

Object.defineProperty(window, 'localStorage', {
    value: localStorageMock,
});

// Mock console methods to avoid noise in tests
const consoleMock = {
    error: vi.fn(),
    warn: vi.fn(),
};

Object.defineProperty(console, 'error', {
    value: consoleMock.error,
});

describe('tokenStorage', () => {
    const mockUser: StoredUser = {
        id: '1',
        name: 'John Doe',
        phone: '1234567890',
        balance: 1000,
        createdAt: '2024-01-01T00:00:00Z',
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    describe('token operations', () => {
        it('should get token from localStorage', () => {
            localStorageMock.getItem.mockReturnValue('test-token');

            const token = tokenStorage.getToken();

            expect(localStorageMock.getItem).toHaveBeenCalledWith('auth_token');
            expect(token).toBe('test-token');
        });

        it('should return null when token does not exist', () => {
            localStorageMock.getItem.mockReturnValue(null);

            const token = tokenStorage.getToken();

            expect(token).toBeNull();
        });

        it('should handle localStorage errors when getting token', () => {
            localStorageMock.getItem.mockImplementation(() => {
                throw new Error('localStorage error');
            });

            const token = tokenStorage.getToken();

            expect(token).toBeNull();
            expect(consoleMock.error).toHaveBeenCalledWith(
                'Error getting token from storage:',
                expect.any(Error)
            );
        });

        it('should set token in localStorage', () => {
            tokenStorage.setToken('new-token');

            expect(localStorageMock.setItem).toHaveBeenCalledWith('auth_token', 'new-token');
        });

        it('should handle localStorage errors when setting token', () => {
            localStorageMock.setItem.mockImplementation(() => {
                throw new Error('localStorage error');
            });

            tokenStorage.setToken('new-token');

            expect(consoleMock.error).toHaveBeenCalledWith(
                'Error setting token in storage:',
                expect.any(Error)
            );
        });
    });

    describe('refresh token operations', () => {
        it('should get refresh token from localStorage', () => {
            localStorageMock.getItem.mockReturnValue('refresh-token');

            const refreshToken = tokenStorage.getRefreshToken();

            expect(localStorageMock.getItem).toHaveBeenCalledWith('refresh_token');
            expect(refreshToken).toBe('refresh-token');
        });

        it('should set refresh token in localStorage', () => {
            tokenStorage.setRefreshToken('new-refresh-token');

            expect(localStorageMock.setItem).toHaveBeenCalledWith('refresh_token', 'new-refresh-token');
        });
    });

    describe('user operations', () => {
        it('should get user from localStorage', () => {
            localStorageMock.getItem.mockReturnValue(JSON.stringify(mockUser));

            const user = tokenStorage.getUser();

            expect(localStorageMock.getItem).toHaveBeenCalledWith('user_data');
            expect(user).toEqual(mockUser);
        });

        it('should return null when user does not exist', () => {
            localStorageMock.getItem.mockReturnValue(null);

            const user = tokenStorage.getUser();

            expect(user).toBeNull();
        });

        it('should handle JSON parsing errors when getting user', () => {
            localStorageMock.getItem.mockReturnValue('invalid-json');

            const user = tokenStorage.getUser();

            expect(user).toBeNull();
            expect(consoleMock.error).toHaveBeenCalledWith(
                'Error getting user from storage:',
                expect.any(Error)
            );
        });

        it('should set user in localStorage', () => {
            tokenStorage.setUser(mockUser);

            expect(localStorageMock.setItem).toHaveBeenCalledWith(
                'user_data',
                JSON.stringify(mockUser)
            );
        });

        it('should handle localStorage errors when setting user', () => {
            localStorageMock.setItem.mockImplementation(() => {
                throw new Error('localStorage error');
            });

            tokenStorage.setUser(mockUser);

            expect(consoleMock.error).toHaveBeenCalledWith(
                'Error setting user in storage:',
                expect.any(Error)
            );
        });
    });

    describe('combined auth data operations', () => {
        it('should set complete auth data', () => {
            tokenStorage.setAuthData('token', mockUser, 'refresh-token');

            expect(localStorageMock.setItem).toHaveBeenCalledWith('auth_token', 'token');
            expect(localStorageMock.setItem).toHaveBeenCalledWith('user_data', JSON.stringify(mockUser));
            expect(localStorageMock.setItem).toHaveBeenCalledWith('refresh_token', 'refresh-token');
        });

        it('should set auth data without refresh token', () => {
            tokenStorage.setAuthData('token', mockUser);

            expect(localStorageMock.setItem).toHaveBeenCalledWith('auth_token', 'token');
            expect(localStorageMock.setItem).toHaveBeenCalledWith('user_data', JSON.stringify(mockUser));
            expect(localStorageMock.setItem).not.toHaveBeenCalledWith('refresh_token', expect.anything());
        });

        it('should clear all auth data', () => {
            tokenStorage.clearAuthData();

            expect(localStorageMock.removeItem).toHaveBeenCalledWith('auth_token');
            expect(localStorageMock.removeItem).toHaveBeenCalledWith('user_data');
            expect(localStorageMock.removeItem).toHaveBeenCalledWith('refresh_token');
        });

        it('should handle localStorage errors when clearing auth data', () => {
            localStorageMock.removeItem.mockImplementation(() => {
                throw new Error('localStorage error');
            });

            tokenStorage.clearAuthData();

            expect(consoleMock.error).toHaveBeenCalledWith(
                'Error clearing auth data from storage:',
                expect.any(Error)
            );
        });
    });

    describe('auth data checks', () => {
        it('should check if token exists', () => {
            localStorageMock.getItem.mockReturnValue('token');

            const hasToken = tokenStorage.hasToken();

            expect(hasToken).toBe(true);
        });

        it('should return false when token does not exist', () => {
            localStorageMock.getItem.mockReturnValue(null);

            const hasToken = tokenStorage.hasToken();

            expect(hasToken).toBe(false);
        });

        it('should check if user exists', () => {
            localStorageMock.getItem.mockReturnValue(JSON.stringify(mockUser));

            const hasUser = tokenStorage.hasUser();

            expect(hasUser).toBe(true);
        });

        it('should return false when user does not exist', () => {
            localStorageMock.getItem.mockReturnValue(null);

            const hasUser = tokenStorage.hasUser();

            expect(hasUser).toBe(false);
        });

        it('should check if complete auth data exists', () => {
            localStorageMock.getItem.mockImplementation((key) => {
                if (key === 'auth_token') return 'token';
                if (key === 'user_data') return JSON.stringify(mockUser);
                return null;
            });

            const hasCompleteAuth = tokenStorage.hasCompleteAuthData();

            expect(hasCompleteAuth).toBe(true);
        });

        it('should return false when auth data is incomplete', () => {
            localStorageMock.getItem.mockImplementation((key) => {
                if (key === 'auth_token') return 'token';
                if (key === 'user_data') return null; // Missing user data
                return null;
            });

            const hasCompleteAuth = tokenStorage.hasCompleteAuthData();

            expect(hasCompleteAuth).toBe(false);
        });
    });
});

describe('jwtUtils', () => {
    // Mock JWT token (header.payload.signature)
    const createMockJWT = (payload: any) => {
        const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
        const encodedPayload = btoa(JSON.stringify(payload));
        const signature = 'mock-signature';
        return `${header}.${encodedPayload}.${signature}`;
    };

    describe('decodeToken', () => {
        it('should decode valid JWT token', () => {
            const payload = { sub: '1', exp: 1234567890 };
            const token = createMockJWT(payload);

            const decoded = jwtUtils.decodeToken(token);

            expect(decoded).toEqual(payload);
        });

        it('should handle invalid JWT token', () => {
            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => { });

            const decoded = jwtUtils.decodeToken('invalid-token');

            expect(decoded).toBeNull();
            expect(consoleSpy).toHaveBeenCalledWith('Error decoding token:', expect.any(Error));

            consoleSpy.mockRestore();
        });

        it('should handle malformed JWT token', () => {
            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => { });

            const decoded = jwtUtils.decodeToken('header.invalid-base64.signature');

            expect(decoded).toBeNull();
            expect(consoleSpy).toHaveBeenCalledWith('Error decoding token:', expect.any(Error));

            consoleSpy.mockRestore();
        });
    });

    describe('isTokenExpired', () => {
        it('should return false for non-expired token', () => {
            const futureTime = Math.floor(Date.now() / 1000) + 3600; // 1 hour from now
            const payload = { exp: futureTime };
            const token = createMockJWT(payload);

            const isExpired = jwtUtils.isTokenExpired(token);

            expect(isExpired).toBe(false);
        });

        it('should return true for expired token', () => {
            const pastTime = Math.floor(Date.now() / 1000) - 3600; // 1 hour ago
            const payload = { exp: pastTime };
            const token = createMockJWT(payload);

            const isExpired = jwtUtils.isTokenExpired(token);

            expect(isExpired).toBe(true);
        });

        it('should return true for token without expiration', () => {
            const payload = { sub: '1' }; // No exp field
            const token = createMockJWT(payload);

            const isExpired = jwtUtils.isTokenExpired(token);

            expect(isExpired).toBe(true);
        });

        it('should return true for invalid token', () => {
            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => { });

            const isExpired = jwtUtils.isTokenExpired('invalid-token');

            expect(isExpired).toBe(true);
            expect(consoleSpy).toHaveBeenCalled();

            consoleSpy.mockRestore();
        });
    });

    describe('getTokenExpiration', () => {
        it('should return expiration date for valid token', () => {
            const expTime = Math.floor(Date.now() / 1000) + 3600;
            const payload = { exp: expTime };
            const token = createMockJWT(payload);

            const expiration = jwtUtils.getTokenExpiration(token);

            expect(expiration).toBeInstanceOf(Date);
            expect(expiration?.getTime()).toBe(expTime * 1000);
        });

        it('should return null for token without expiration', () => {
            const payload = { sub: '1' };
            const token = createMockJWT(payload);

            const expiration = jwtUtils.getTokenExpiration(token);

            expect(expiration).toBeNull();
        });

        it('should return null for invalid token', () => {
            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => { });

            const expiration = jwtUtils.getTokenExpiration('invalid-token');

            expect(expiration).toBeNull();
            expect(consoleSpy).toHaveBeenCalled();

            consoleSpy.mockRestore();
        });
    });

    describe('willTokenExpireSoon', () => {
        it('should return false for token that will not expire soon', () => {
            const futureTime = Math.floor(Date.now() / 1000) + 3600; // 1 hour from now
            const payload = { exp: futureTime };
            const token = createMockJWT(payload);

            const willExpireSoon = jwtUtils.willTokenExpireSoon(token, 5); // 5 minutes threshold

            expect(willExpireSoon).toBe(false);
        });

        it('should return true for token that will expire soon', () => {
            const soonTime = Math.floor(Date.now() / 1000) + 120; // 2 minutes from now
            const payload = { exp: soonTime };
            const token = createMockJWT(payload);

            const willExpireSoon = jwtUtils.willTokenExpireSoon(token, 5); // 5 minutes threshold

            expect(willExpireSoon).toBe(true);
        });

        it('should use default threshold of 5 minutes', () => {
            const soonTime = Math.floor(Date.now() / 1000) + 120; // 2 minutes from now
            const payload = { exp: soonTime };
            const token = createMockJWT(payload);

            const willExpireSoon = jwtUtils.willTokenExpireSoon(token); // No threshold specified

            expect(willExpireSoon).toBe(true);
        });

        it('should return true for token without expiration', () => {
            const payload = { sub: '1' };
            const token = createMockJWT(payload);

            const willExpireSoon = jwtUtils.willTokenExpireSoon(token);

            expect(willExpireSoon).toBe(true);
        });

        it('should return true for invalid token', () => {
            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => { });

            const willExpireSoon = jwtUtils.willTokenExpireSoon('invalid-token');

            expect(willExpireSoon).toBe(true);
            expect(consoleSpy).toHaveBeenCalled();

            consoleSpy.mockRestore();
        });
    });
});