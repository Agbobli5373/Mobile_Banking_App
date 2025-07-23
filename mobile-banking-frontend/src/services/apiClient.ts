import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import type { ApiError } from '../types/api';

// Create a base axios instance with common configuration
const apiClient: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/api',
    timeout: 15000, // 15 seconds timeout
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    },
});

// Request interceptor for adding auth token
apiClient.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = localStorage.getItem('auth_token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(new Error(error.message || 'Request failed'));
    }
);

// Response interceptor for handling common errors
apiClient.interceptors.response.use(
    (response: AxiosResponse) => {
        return response;
    },
    (error) => {
        // Handle authentication errors
        if (error.response?.status === 401) {
            // Clear token and redirect to login
            localStorage.removeItem('auth_token');
            window.location.href = '/login';
        }

        // Transform error to consistent format
        const apiError: ApiError = {
            status: error.response?.status || 500,
            message: error.response?.data?.message || error.message || 'An error occurred',
            timestamp: new Date().toISOString(),
            path: error.config?.url || '',
        };

        return Promise.reject(apiError);
    }
);

export default apiClient;