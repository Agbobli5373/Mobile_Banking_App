import apiClient from './apiClient';
import type {
    Transaction,
    TransactionHistory,
    TransactionFilters,
    ApiResponse
} from '../types/api';

export class TransactionService {
    /**
     * Get transaction history with optional filtering and pagination
     */
    async getTransactions(filters: TransactionFilters = {}): Promise<TransactionHistory> {
        const params = new URLSearchParams();

        // Add pagination parameters
        if (filters.page !== undefined) {
            params.append('page', filters.page.toString());
        }
        if (filters.pageSize !== undefined) {
            params.append('pageSize', filters.pageSize.toString());
        }

        // Add filter parameters
        if (filters.type && filters.type !== 'ALL') {
            params.append('type', filters.type);
        }
        if (filters.startDate) {
            params.append('startDate', filters.startDate);
        }
        if (filters.endDate) {
            params.append('endDate', filters.endDate);
        }
        if (filters.minAmount !== undefined) {
            params.append('minAmount', filters.minAmount.toString());
        }
        if (filters.maxAmount !== undefined) {
            params.append('maxAmount', filters.maxAmount.toString());
        }

        const queryString = params.toString();
        const url = `/transactions${queryString ? `?${queryString}` : ''}`;

        const response = await apiClient.get<ApiResponse<TransactionHistory>>(url);
        return response.data.data;
    }

    /**
     * Get a specific transaction by ID
     */
    async getTransaction(transactionId: string): Promise<Transaction> {
        const response = await apiClient.get<ApiResponse<Transaction>>(`/transactions/${transactionId}`);
        return response.data.data;
    }

    /**
     * Get recent transactions (last 10)
     */
    async getRecentTransactions(): Promise<Transaction[]> {
        const response = await this.getTransactions({ page: 0, pageSize: 10 });
        return response.transactions;
    }

    /**
     * Search transactions by recipient name or phone
     */
    async searchTransactions(query: string, filters: TransactionFilters = {}): Promise<TransactionHistory> {
        const params = new URLSearchParams();
        params.append('search', query);

        // Add other filters
        if (filters.page !== undefined) {
            params.append('page', filters.page.toString());
        }
        if (filters.pageSize !== undefined) {
            params.append('pageSize', filters.pageSize.toString());
        }
        if (filters.type && filters.type !== 'ALL') {
            params.append('type', filters.type);
        }

        const response = await apiClient.get<ApiResponse<TransactionHistory>>(`/transactions/search?${params.toString()}`);
        return response.data.data;
    }

    /**
     * Get transaction statistics for a date range
     */
    async getTransactionStats(startDate?: string, endDate?: string): Promise<{
        totalTransactions: number;
        totalSent: number;
        totalReceived: number;
        totalDeposits: number;
    }> {
        const params = new URLSearchParams();
        if (startDate) params.append('startDate', startDate);
        if (endDate) params.append('endDate', endDate);

        const response = await apiClient.get<ApiResponse<{
            totalTransactions: number;
            totalSent: number;
            totalReceived: number;
            totalDeposits: number;
        }>>(`/transactions/stats?${params.toString()}`);

        return response.data.data;
    }

    /**
     * Validate transaction filters
     */
    validateFilters(filters: TransactionFilters): { isValid: boolean; errors: string[] } {
        const errors: string[] = [];

        // Validate page number
        if (filters.page !== undefined && filters.page < 0) {
            errors.push('Page number must be 0 or greater');
        }

        // Validate page size
        if (filters.pageSize !== undefined) {
            if (filters.pageSize < 1) {
                errors.push('Page size must be at least 1');
            } else if (filters.pageSize > 100) {
                errors.push('Page size cannot exceed 100');
            }
        }

        // Validate date range
        if (filters.startDate && filters.endDate) {
            const startDate = new Date(filters.startDate);
            const endDate = new Date(filters.endDate);

            if (startDate > endDate) {
                errors.push('Start date must be before end date');
            }
        }

        // Validate amount range
        if (filters.minAmount !== undefined && filters.minAmount < 0) {
            errors.push('Minimum amount must be 0 or greater');
        }

        if (filters.maxAmount !== undefined && filters.maxAmount < 0) {
            errors.push('Maximum amount must be 0 or greater');
        }

        if (filters.minAmount !== undefined && filters.maxAmount !== undefined) {
            if (filters.minAmount > filters.maxAmount) {
                errors.push('Minimum amount must be less than or equal to maximum amount');
            }
        }

        return {
            isValid: errors.length === 0,
            errors
        };
    }

    /**
     * Format transaction for display
     */
    formatTransaction(transaction: Transaction, currentUserId: string): {
        displayName: string;
        displayAmount: string;
        isIncoming: boolean;
        transactionType: 'sent' | 'received' | 'deposit';
        formattedDate: string;
    } {
        const isIncoming = transaction.receiverId === currentUserId;
        const isDeposit = transaction.type === 'DEPOSIT';

        let displayName: string;
        let transactionType: 'sent' | 'received' | 'deposit';

        if (isDeposit) {
            displayName = 'Deposit';
            transactionType = 'deposit';
        } else if (isIncoming) {
            displayName = transaction.senderName || 'Unknown Sender';
            transactionType = 'received';
        } else {
            displayName = transaction.receiverName || 'Unknown Recipient';
            transactionType = 'sent';
        }

        const displayAmount = this.formatCurrency(transaction.amount);
        const formattedDate = this.formatTransactionDate(transaction.timestamp);

        return {
            displayName,
            displayAmount,
            isIncoming: isIncoming || isDeposit,
            transactionType,
            formattedDate
        };
    }

    /**
     * Format currency amount
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
     * Format transaction date for display
     */
    formatTransactionDate(timestamp: string): string {
        const date = new Date(timestamp);
        const now = new Date();
        const diffInHours = (now.getTime() - date.getTime()) / (1000 * 60 * 60);

        if (diffInHours < 1) {
            const diffInMinutes = Math.floor(diffInHours * 60);
            return diffInMinutes <= 1 ? 'Just now' : `${diffInMinutes} minutes ago`;
        } else if (diffInHours < 24) {
            const hours = Math.floor(diffInHours);
            return `${hours} hour${hours === 1 ? '' : 's'} ago`;
        } else if (diffInHours < 48) {
            return 'Yesterday';
        } else {
            return date.toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
                year: date.getFullYear() !== now.getFullYear() ? 'numeric' : undefined,
            });
        }
    }

    /**
     * Get default filters for transaction list
     */
    getDefaultFilters(): TransactionFilters {
        return {
            page: 0,
            pageSize: 20,
            type: 'ALL',
        };
    }

    /**
     * Create date range filters for common periods
     */
    createDateRangeFilter(period: 'today' | 'week' | 'month' | 'year'): {
        startDate: string;
        endDate: string;
    } {
        const now = new Date();
        const startDate = new Date();

        switch (period) {
            case 'today':
                startDate.setHours(0, 0, 0, 0);
                break;
            case 'week':
                startDate.setDate(now.getDate() - 7);
                break;
            case 'month':
                startDate.setMonth(now.getMonth() - 1);
                break;
            case 'year':
                startDate.setFullYear(now.getFullYear() - 1);
                break;
        }

        return {
            startDate: startDate.toISOString().split('T')[0],
            endDate: now.toISOString().split('T')[0],
        };
    }

    /**
     * Group transactions by date
     */
    groupTransactionsByDate(transactions: Transaction[]): Record<string, Transaction[]> {
        return transactions.reduce((groups, transaction) => {
            const date = new Date(transaction.timestamp).toDateString();
            if (!groups[date]) {
                groups[date] = [];
            }
            groups[date].push(transaction);
            return groups;
        }, {} as Record<string, Transaction[]>);
    }

    /**
     * Calculate transaction totals
     */
    calculateTotals(transactions: Transaction[], currentUserId: string): {
        totalSent: number;
        totalReceived: number;
        totalDeposits: number;
        netAmount: number;
    } {
        let totalSent = 0;
        let totalReceived = 0;
        let totalDeposits = 0;

        transactions.forEach(transaction => {
            if (transaction.type === 'DEPOSIT') {
                totalDeposits += transaction.amount;
            } else if (transaction.senderId === currentUserId) {
                totalSent += transaction.amount;
            } else if (transaction.receiverId === currentUserId) {
                totalReceived += transaction.amount;
            }
        });

        const netAmount = totalReceived + totalDeposits - totalSent;

        return {
            totalSent,
            totalReceived,
            totalDeposits,
            netAmount,
        };
    }
}

// Export singleton instance
export const transactionService = new TransactionService();