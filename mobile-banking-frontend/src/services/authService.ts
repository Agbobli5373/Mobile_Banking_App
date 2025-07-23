import apiClient from './apiClient';
import type {
    LoginRequest,
    RegisterRequest,
    AuthResponse,
    User,
    ApiResponse
} from '../types/api';

export class AuthService {
    /**
     * Login user with phone and PIN
     */
    async login(credentials: LoginRequest): Promise<AuthResponse> {
        const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', credentials);
        console.log(response)

        // Store token in localStorage
        if (response.data.data.token) {
            localStorage.setItem('auth_token', response.data.data.token);
            localStorage.setItem('user', JSON.stringify(response.data.data.user));
        }

        return response.data.data;
    }

    /**
     * Register new user
     */
    async register(userData: RegisterRequest): Promise<AuthResponse> {
        const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', userData);

        // use register response to login user
        if (response.data) {
           this.login({ phoneNumber: userData.phoneNumber, pin: userData.pin });
        }

        return response.data.data;
    }

    /**
     * Refresh authentication token
     */
    async refreshToken(): Promise<AuthResponse> {
        const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/refresh');

        // Update stored token
        if (response.data.data.token) {
            localStorage.setItem('auth_token', response.data.data.token);
            localStorage.setItem('user', JSON.stringify(response.data.data.user));
        }

        return response.data.data;
    }

    /**
     * Logout user and clear stored data
     */
    async logout(): Promise<void> {
        try {
            // Call logout endpoint to invalidate token on server
            await apiClient.post('/auth/logout');
        } catch (error) {
            // Continue with local cleanup even if server call fails
            console.warn('Logout API call failed:', error);
        } finally {
            // Always clear local storage
            this.clearAuthData();
        }
    }

    /**
     * Get current user from localStorage
     */
    getCurrentUser(): User | null {
        try {
            const userStr = localStorage.getItem('user');
            return userStr ? JSON.parse(userStr) : null;
        } catch (error) {
            console.error('Error parsing user data:', error);
            return null;
        }
    }

    /**
     * Get current auth token
     */
    getToken(): string | null {
        return localStorage.getItem('auth_token');
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated(): boolean {
        const token = this.getToken();
        const user = this.getCurrentUser();
        return !!(token && user);
    }

    /**
     * Check if token is expired
     */
    isTokenExpired(): boolean {
        try {
            const token = this.getToken();
            if (!token) return true;

            // Decode JWT token to check expiration
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Date.now() / 1000;

            return payload.exp < currentTime;
        } catch (error) {
            console.error('Error checking token expiration:', error);
            return true;
        }
    }

    /**
     * Clear authentication data from localStorage
     */
    clearAuthData(): void {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user');
    }

    /**
     * Validate credentials format before sending to server
     */
    validateCredentials(credentials: LoginRequest): { isValid: boolean; errors: string[] } {
        const errors: string[] = [];

        if (!credentials.phoneNumber || credentials.phoneNumber.trim().length === 0) {
            errors.push('Phone number is required');
        } else if (!/^\d{10,}$/.test(credentials.phoneNumber.replace(/\D/g, ''))) {
            errors.push('Phone number must be at least 10 digits');
        }

        if (!credentials.pin || credentials.pin.length !== 4) {
            errors.push('PIN must be exactly 4 digits');
        } else if (!/^\d{4}$/.test(credentials.pin)) {
            errors.push('PIN must contain only digits');
        }

        return {
            isValid: errors.length === 0,
            errors
        };
    }

    /**
     * Validate registration data format
     */
    validateRegistration(userData: RegisterRequest): { isValid: boolean; errors: string[] } {
        const errors: string[] = [];

        if (!userData.name || userData.name.trim().length === 0) {
            errors.push('Name is required');
        } else if (userData.name.trim().length < 2) {
            errors.push('Name must be at least 2 characters');
        }

        if (!userData.phoneNumber || userData.phoneNumber.trim().length === 0) {
            errors.push('Phone number is required');
        } else if (!/^\d{10,}$/.test(userData.phoneNumber.replace(/\D/g, ''))) {
            errors.push('Phone number must be at least 10 digits');
        }

        if (!userData.pin || userData.pin.length !== 4) {
            errors.push('PIN must be exactly 4 digits');
        } else if (!/^\d{4}$/.test(userData.pin)) {
            errors.push('PIN must contain only digits');
        }

        return {
            isValid: errors.length === 0,
            errors
        };
    }
}

// Export singleton instance
export const authService = new AuthService();