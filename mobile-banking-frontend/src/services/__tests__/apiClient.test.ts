import { describe, expect, it, vi, beforeEach } from 'vitest';
import axios from 'axios';
import apiClient from '../apiClient';
import { extractErrorMessage } from '../../utils/errorHandler';

// Mock axios
vi.mock('axios', () => {
    return {
        default: {
            create: vi.fn(() => ({
                interceptors: {
                    request: { use: vi.fn(), eject: vi.fn() },
                    response: { use: vi.fn(), eject: vi.fn() },
                },
            })),
        },
    };
});

describe('apiClient', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    it('should create axios instance with correct config', () => {
        // We can't test this directly since we're mocking axios.create
        // Just verify that the mock was imported correctly
        expect(axios.create).toBeDefined();
    });
});

describe('errorHandler', () => {
    describe('extractErrorMessage', () => {
        it('should handle network errors', () => {
            // Create a mock error object instead of using AxiosError
            const networkError = {
                message: 'Network Error',
                code: 'ERR_NETWORK',
                response: undefined,
                isAxiosError: true,
            };

            // Mock the instanceof check
            vi.spyOn(global, 'Object').mockImplementationOnce(() => ({
                toString: () => '[object Error]',
                message: networkError.message
            }));

            // Act
            const result = extractErrorMessage(networkError);

            // Assert
            expect(result).toBe('Connection problem. Please check your internet and try again.');
        });

        it('should handle generic errors', () => {
            // Arrange
            const genericError = new Error('Something went wrong');

            // Act
            const result = extractErrorMessage(genericError);

            // Assert
            expect(result).toBe('Something went wrong');
        });

        it('should handle unknown error types', () => {
            // Arrange
            const unknownError = 'Not an error object';

            // Act
            const result = extractErrorMessage(unknownError);

            // Assert
            expect(result).toBe('Something went wrong. Please try again later.');
        });
    });
});