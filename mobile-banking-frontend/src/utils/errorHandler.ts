import type { ApiError } from '../types';

// User-friendly error messages for common errors
export const errorMessages = {
    INSUFFICIENT_FUNDS: "You don't have enough balance for this transfer.",
    USER_NOT_FOUND: "The recipient phone number was not found.",
    INVALID_CREDENTIALS: "Invalid phone number or PIN. Please try again.",
    NETWORK_ERROR: "Connection problem. Please check your internet and try again.",
    DUPLICATE_PHONE: "This phone number is already registered.",
    INVALID_AMOUNT: "Please enter a valid amount.",
    INVALID_PHONE: "Please enter a valid phone number.",
    INVALID_PIN: "PIN must be exactly 4 digits.",
    SELF_TRANSFER: "You cannot transfer money to yourself.",
    GENERAL_ERROR: "Something went wrong. Please try again later.",
};

// Error code to message mapping
export const getErrorMessage = (errorCode: string): string => {
    return errorMessages[errorCode as keyof typeof errorMessages] || errorMessages.GENERAL_ERROR;
};

// Extract error message from API error response
export const extractErrorMessage = (error: unknown): string => {
    // Check if it's an Axios error
    if (error && typeof error === 'object' && 'isAxiosError' in error && error.isAxiosError) {
        const axiosError = error as any;

        // Handle network errors
        if (!axiosError.response) {
            return errorMessages.NETWORK_ERROR;
        }

        // Handle API errors with error codes
        const apiError = axiosError.response.data as ApiError;
        if (apiError?.message) {
            // Check if the message is an error code we can map
            if (Object.keys(errorMessages).includes(apiError.message)) {
                return getErrorMessage(apiError.message);
            }
            return apiError.message;
        }

        // Handle HTTP status code errors
        switch (axiosError.response.status) {
            case 400:
                return 'Invalid request. Please check your input.';
            case 401:
                return 'Authentication failed. Please log in again.';
            case 403:
                return 'You do not have permission to perform this action.';
            case 404:
                return 'The requested resource was not found.';
            case 500:
                return 'Server error. Please try again later.';
            default:
                return errorMessages.GENERAL_ERROR;
        }
    }

    // Handle other types of errors
    if (error instanceof Error) {
        return error.message;
    }

    return errorMessages.GENERAL_ERROR;
};