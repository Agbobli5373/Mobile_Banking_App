import apiClient from './apiClient';
import type {
    TransferRequest,
    AddFundsRequest,
    User,
    ApiResponse
} from '../types/api';

export class WalletService {
    /**
     * Get current user balance
     */
    async getBalance(): Promise<number> {
        const response = await apiClient.get<ApiResponse<{ balance: number }>>('/wallet/balance');
        return response.data.data.balance;
    }

    /**
     * Transfer money to another user
     */
    async transfer(transferData: TransferRequest): Promise<{
        success: boolean;
        newBalance: number;
        transactionId: string;
    }> {
        const response = await apiClient.post<ApiResponse<{
            success: boolean;
            newBalance: number;
            transactionId: string;
        }>>('/wallet/transfer', transferData);

        return response.data.data;
    }

    /**
     * Add funds to user wallet
     */
    async addFunds(fundsData: AddFundsRequest): Promise<{
        success: boolean;
        newBalance: number;
        transactionId: string;
    }> {
        const response = await apiClient.post<ApiResponse<{
            success: boolean;
            newBalance: number;
            transactionId: string;
        }>>('/wallet/add-funds', fundsData);

        return response.data.data;
    }

    /**
     * Get user profile with balance
     */
    async getUserProfile(): Promise<User> {
        const response = await apiClient.get<ApiResponse<User>>('/wallet/profile');
        return response.data.data;
    }

    /**
     * Validate transfer request
     */
    validateTransfer(transferData: TransferRequest, currentBalance?: number): {
        isValid: boolean;
        errors: string[]
    } {
        const errors: string[] = [];

        // Validate recipient phone
        if (!transferData.recipientPhone || transferData.recipientPhone.trim().length === 0) {
            errors.push('Recipient phone number is required');
        } else if (!/^\d{10,}$/.test(transferData.recipientPhone.replace(/\D/g, ''))) {
            errors.push('Recipient phone number must be at least 10 digits');
        }

        // Validate amount
        if (!transferData.amount || transferData.amount <= 0) {
            errors.push('Transfer amount must be greater than 0');
        } else if (transferData.amount > 10000) {
            errors.push('Maximum transfer amount is $10,000');
        } else if (transferData.amount < 0.01) {
            errors.push('Minimum transfer amount is $0.01');
        }

        // Check if amount has more than 2 decimal places
        if (transferData.amount && (transferData.amount * 100) % 1 !== 0) {
            errors.push('Amount cannot have more than 2 decimal places');
        }

        // Validate against current balance if provided
        if (currentBalance !== undefined && transferData.amount > currentBalance) {
            errors.push('Insufficient funds for this transfer');
        }

        return {
            isValid: errors.length === 0,
            errors
        };
    }

    /**
     * Validate add funds request
     */
    validateAddFunds(fundsData: AddFundsRequest): {
        isValid: boolean;
        errors: string[]
    } {
        const errors: string[] = [];

        // Validate amount
        if (!fundsData.amount || fundsData.amount <= 0) {
            errors.push('Amount must be greater than 0');
        } else if (fundsData.amount > 10000) {
            errors.push('Maximum add funds amount is $10,000');
        } else if (fundsData.amount < 1) {
            errors.push('Minimum add funds amount is $1');
        }

        // Check if amount has more than 2 decimal places
        if (fundsData.amount && (fundsData.amount * 100) % 1 !== 0) {
            errors.push('Amount cannot have more than 2 decimal places');
        }

        return {
            isValid: errors.length === 0,
            errors
        };
    }

    /**
     * Check if transfer is to self
     */
    isTransferToSelf(recipientPhone: string, currentUserPhone?: string): boolean {
        if (!currentUserPhone) return false;

        // Normalize phone numbers for comparison
        const normalizePhone = (phone: string) => phone.replace(/\D/g, '');

        return normalizePhone(recipientPhone) === normalizePhone(currentUserPhone);
    }

    /**
     * Format currency amount for display
     */
    formatCurrency(amount: number, currency = 'USD'): string {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency,
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
        }).format(amount);
    }

    /**
     * Parse currency string to number
     */
    parseCurrency(currencyString: string): number {
        // Remove currency symbols and parse
        const numericString = currencyString.replace(/[^0-9.-]/g, '');
        const amount = parseFloat(numericString);
        return isNaN(amount) ? 0 : amount;
    }

    /**
     * Validate phone number format
     */
    validatePhoneNumber(phone: string): { isValid: boolean; formatted: string } {
        // Remove all non-digit characters
        const digitsOnly = phone.replace(/\D/g, '');

        // Check if it's a valid length (at least 10 digits)
        const isValid = digitsOnly.length >= 10;

        // Format as needed (you can customize this based on your requirements)
        let formatted = digitsOnly;
        if (digitsOnly.length === 10) {
            // Format as (XXX) XXX-XXXX for US numbers
            formatted = `(${digitsOnly.slice(0, 3)}) ${digitsOnly.slice(3, 6)}-${digitsOnly.slice(6)}`;
        }

        return {
            isValid,
            formatted: isValid ? formatted : phone
        };
    }

    /**
     * Calculate transfer fee (if applicable)
     */
    calculateTransferFee(amount: number): number {
        // For now, no fees. This can be extended based on business rules
        return 0;
    }

    /**
     * Get transfer limits for current user
     */
    getTransferLimits(): {
        minAmount: number;
        maxAmount: number;
        dailyLimit: number;
    } {
        // These could be fetched from the server or configured per user
        return {
            minAmount: 0.01,
            maxAmount: 10000,
            dailyLimit: 25000,
        };
    }
}

// Export singleton instance
export const walletService = new WalletService();