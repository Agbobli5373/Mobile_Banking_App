import type { ApiError } from '../types/api';

// User-friendly error messages mapping
export const errorMessages = {
    INSUFFICIENT_FUNDS: "You don't have enough balance for this transfer.",
    USER_NOT_FOUND: "The recipient phone number was not found.",
    INVALID_CREDENTIALS: "Invalid phone number or PIN. Please try again.",
    NETWORK_ERROR: "Connection problem. Please check your internet and try again.",
    DUPLICATE_PHONE: "This phone number is already registered.",
    INVALID_AMOUNT: "Please enter a valid amount.",
    TRANSFER_TO_SELF: "You cannot transfer money to yourself.",
    SERVER_ERROR: "Something went wrong on our end. Please try again later.",
    VALIDATION_ERROR: "Please check your input and try again.",
    TIMEOUT_ERROR: "Request timed out. Please try again.",
} as const;

// Map API error codes to user-friendly messages
export const getErrorMessage = (error: ApiError | Error | unknown): string => {
    if (error instanceof Error) {
        return error.message;
    }

    if (typeof error === 'object' && error !== null && 'message' in error) {
        const apiError = error as ApiError;

        // Check for specific error messages from backend
        const message = apiError.message?.toLowerCase() || '';

        if (message.includes('insufficient funds') || message.includes('insufficient balance')) {
            return errorMessages.INSUFFICIENT_FUNDS;
        }

        if (message.includes('user not found') || message.includes('recipient not found')) {
            return errorMessages.USER_NOT_FOUND;
        }

        if (message.includes('invalid credentials') || message.includes('authentication failed')) {
            return errorMessages.INVALID_CREDENTIALS;
        }

        if (message.includes('duplicate') && message.includes('phone')) {
            return errorMessages.DUPLICATE_PHONE;
        }

        if (message.includes('invalid amount') || message.includes('amount must be')) {
            return errorMessages.INVALID_AMOUNT;
        }

        if (message.includes('cannot transfer to yourself') || message.includes('self transfer')) {
            return errorMessages.TRANSFER_TO_SELF;
        }

        if (message.includes('validation') || message.includes('invalid input')) {
            return errorMessages.VALIDATION_ERROR;
        }

        if (message.includes('timeout') || message.includes('timed out')) {
            return errorMessages.TIMEOUT_ERROR;
        }

        // Handle HTTP status codes
        if ('status' in apiError) {
            switch (apiError.status) {
                case 400:
                    return errorMessages.VALIDATION_ERROR;
                case 401:
                    return errorMessages.INVALID_CREDENTIALS;
                case 404:
                    return errorMessages.USER_NOT_FOUND;
                case 409:
                    return errorMessages.DUPLICATE_PHONE;
                case 500:
                case 502:
                case 503:
                    return errorMessages.SERVER_ERROR;
                case 408:
                    return errorMessages.TIMEOUT_ERROR;
                default:
                    return apiError.message || errorMessages.SERVER_ERROR;
            }
        }

        return apiError.message || errorMessages.SERVER_ERROR;
    }

    return errorMessages.SERVER_ERROR;
};

// Check if error is a network error
export const isNetworkError = (error: unknown): boolean => {
    if (error instanceof Error) {
        return error.message.includes('Network Error') ||
            error.message.includes('timeout') ||
            error.message.includes('ECONNREFUSED');
    }

    if (typeof error === 'object' && error !== null && 'code' in error) {
        const code = (error as any).code;
        return code === 'NETWORK_ERROR' || code === 'ECONNREFUSED' || code === 'TIMEOUT';
    }

    return false;
};

// Check if error should trigger a retry
export const shouldRetry = (error: unknown): boolean => {
    if (isNetworkError(error)) {
        return true;
    }

    if (typeof error === 'object' && error !== null && 'status' in error) {
        const status = (error as ApiError).status;
        // Retry on server errors but not client errors
        return status >= 500 || status === 408; // 408 is timeout
    }

    return false;
};

// Format error for logging
export const formatErrorForLogging = (error: unknown, context?: string): string => {
    const timestamp = new Date().toISOString();
    const contextStr = context ? `[${context}] ` : '';

    if (error instanceof Error) {
        return `${timestamp} ${contextStr}Error: ${error.message}\nStack: ${error.stack}`;
    }

    if (typeof error === 'object' && error !== null) {
        return `${timestamp} ${contextStr}API Error: ${JSON.stringify(error, null, 2)}`;
    }

    return `${timestamp} ${contextStr}Unknown Error: ${String(error)}`;
};