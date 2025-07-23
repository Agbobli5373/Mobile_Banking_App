import { describe, it, expect } from 'vitest';
import {
    getErrorMessage,
    isNetworkError,
    shouldRetry,
    formatErrorForLogging,
    errorMessages
} from '../errorHandling';
import type { ApiError } from '../../types/api';

describe('Error Handling Utilities', () => {
    describe('getErrorMessage', () => {
        it('should return user-friendly message for insufficient funds', () => {
            const apiError: ApiError = {
                status: 400,
                message: 'Insufficient funds for this transfer',
                timestamp: '2023-01-01T00:00:00Z',
                path: '/api/wallet/transfer',
            };

            const result = getErrorMessage(apiError);

            expect(result).toBe(errorMessages.INSUFFICIENT_FUNDS);
        });

        it('should return user-friendly message for user not found', () => {
            const apiError: ApiError = {
                status: 404,
                message: 'User not found',
                timestamp: '2023-01-01T00:00:00Z',
                path: '/api/users/123',
            };

            const result = getErrorMessage(apiError);

            expect(result).toBe(errorMessages.USER_NOT_FOUND);
        });

        it('should return user-friendly message for invalid credentials', () => {
            const apiError: ApiError = {
                status: 401,
                message: 'Invalid credentials provided',
                timestamp: '2023-01-01T00:00:00Z',
                path: '/api/auth/login',
            };

            const result = getErrorMessage(apiError);

            expect(result).toBe(errorMessages.INVALID_CREDENTIALS);
        });

        it('should return user-friendly message for duplicate phone', () => {
            const apiError: ApiError = {
                status: 409,
                message: 'Duplicate phone number',
                timestamp: '2023-01-01T00:00:00Z',
                path: '/api/auth/register',
            };

            const result = getErrorMessage(apiError);

            expect(result).toBe(errorMessages.DUPLICATE_PHONE);
        });

        it('should return user-friendly message for validation errors', () => {
            const apiError: ApiError = {
                status: 400,
                message: 'Validation failed',
                timestamp: '2023-01-01T00:00:00Z',
                path: '/api/wallet/transfer',
            };

            const result = getErrorMessage(apiError);

            expect(result).toBe(errorMessages.VALIDATION_ERROR);
        });

        it('should return user-friendly message for server errors', () => {
            const apiError: ApiError = {
                status: 500,
                message: 'Internal server error',
                timestamp: '2023-01-01T00:00:00Z',
                path: '/api/wallet/balance',
            };

            const result = getErrorMessage(apiError);

            expect(result).toBe(errorMessages.SERVER_ERROR);
        });

        it('should return user-friendly message for timeout errors', () => {
            const apiError: ApiError = {
                status: 408,
                message: 'Request timeout',
                timestamp: '2023-01-01T00:00:00Z',
                path: '/api/transactions',
            };

            const result = getErrorMessage(apiError);

            expect(result).toBe(errorMessages.TIMEOUT_ERROR);
        });

        it('should handle Error objects', () => {
            const error = new Error('Custom error message');

            const result = getErrorMessage(error);

            expect(result).toBe('Custom error message');
        });

        it('should handle unknown error types', () => {
            const result = getErrorMessage('unknown error');

            expect(result).toBe(errorMessages.SERVER_ERROR);
        });

        it('should return original message if no specific mapping found', () => {
            const apiError: ApiError = {
                status: 418,
                message: 'I am a teapot',
                timestamp: '2023-01-01T00:00:00Z',
                path: '/api/coffee',
            };

            const result = getErrorMessage(apiError);

            expect(result).toBe('I am a teapot');
        });
    });

    describe('isNetworkError', () => {
        it('should detect network error from Error object', () => {
            const error = new Error('Network Error');

            const result = isNetworkError(error);

            expect(result).toBe(true);
        });

        it('should detect timeout error from Error object', () => {
            const error = new Error('Request timeout');

            const result = isNetworkError(error);

            expect(result).toBe(true);
        });

        it('should detect connection refused error from Error object', () => {
            const error = new Error('ECONNREFUSED');

            const result = isNetworkError(error);

            expect(result).toBe(true);
        });

        it('should detect network error from error code', () => {
            const error = { code: 'NETWORK_ERROR' };

            const result = isNetworkError(error);

            expect(result).toBe(true);
        });

        it('should detect connection refused from error code', () => {
            const error = { code: 'ECONNREFUSED' };

            const result = isNetworkError(error);

            expect(result).toBe(true);
        });

        it('should detect timeout from error code', () => {
            const error = { code: 'TIMEOUT' };

            const result = isNetworkError(error);

            expect(result).toBe(true);
        });

        it('should return false for non-network errors', () => {
            const error = new Error('Validation failed');

            const result = isNetworkError(error);

            expect(result).toBe(false);
        });

        it('should return false for unknown error types', () => {
            const result = isNetworkError('unknown error');

            expect(result).toBe(false);
        });
    });

    describe('shouldRetry', () => {
        it('should retry on network errors', () => {
            const error = new Error('Network Error');

            const result = shouldRetry(error);

            expect(result).toBe(true);
        });

        it('should retry on server errors (5xx)', () => {
            const error = { status: 500 };

            const result = shouldRetry(error);

            expect(result).toBe(true);
        });

        it('should retry on timeout errors', () => {
            const error = { status: 408 };

            const result = shouldRetry(error);

            expect(result).toBe(true);
        });

        it('should not retry on client errors (4xx)', () => {
            const error = { status: 400 };

            const result = shouldRetry(error);

            expect(result).toBe(false);
        });

        it('should not retry on authentication errors', () => {
            const error = { status: 401 };

            const result = shouldRetry(error);

            expect(result).toBe(false);
        });

        it('should not retry on unknown errors', () => {
            const result = shouldRetry('unknown error');

            expect(result).toBe(false);
        });
    });

    describe('formatErrorForLogging', () => {
        it('should format Error object for logging', () => {
            const error = new Error('Test error');
            error.stack = 'Error: Test error\n    at test.js:1:1';

            const result = formatErrorForLogging(error, 'TestContext');

            expect(result).toContain('[TestContext]');
            expect(result).toContain('Error: Test error');
            expect(result).toContain('Stack: Error: Test error');
        });

        it('should format API error object for logging', () => {
            const apiError: ApiError = {
                status: 400,
                message: 'Bad request',
                timestamp: '2023-01-01T00:00:00Z',
                path: '/api/test',
            };

            const result = formatErrorForLogging(apiError, 'APIContext');

            expect(result).toContain('[APIContext]');
            expect(result).toContain('API Error:');
            expect(result).toContain('"status": 400');
            expect(result).toContain('"message": "Bad request"');
        });

        it('should format unknown error for logging', () => {
            const result = formatErrorForLogging('unknown error', 'UnknownContext');

            expect(result).toContain('[UnknownContext]');
            expect(result).toContain('Unknown Error: unknown error');
        });

        it('should format error without context', () => {
            const error = new Error('Test error');

            const result = formatErrorForLogging(error);

            expect(result).not.toContain('[');
            expect(result).toContain('Error: Test error');
        });

        it('should include timestamp in formatted error', () => {
            const error = new Error('Test error');

            const result = formatErrorForLogging(error);

            // Check that result starts with a timestamp (ISO format)
            expect(result).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z/);
        });
    });
});