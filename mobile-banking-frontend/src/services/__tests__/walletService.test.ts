import { describe, it, expect, vi, beforeEach } from 'vitest';
import { walletService } from '../walletService';
import apiClient from '../apiClient';
import type { TransferRequest, AddFundsRequest, User } from '../../types/api';

// Mock the API client
vi.mock('../apiClient', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
    },
}));

describe('WalletService', () => {
    const mockUser: User = {
        id: '123',
        name: 'John Doe',
        phoneNumber: '1234567890',
        balance: 1000,
        createdAt: '2023-01-01T00:00:00Z',
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('getBalance', () => {
        it('should return current balance', async () => {
            const mockResponse = {
                data: {
                    data: { balance: 1500 },
                },
            };

            (apiClient.get as any).mockResolvedValue(mockResponse);

            const result = await walletService.getBalance();

            expect(apiClient.get).toHaveBeenCalledWith('/wallet/balance');
            expect(result).toBe(1500);
        });

        it('should handle API error', async () => {
            (apiClient.get as any).mockRejectedValue(new Error('Network error'));

            await expect(walletService.getBalance()).rejects.toThrow('Network error');
        });
    });

    describe('transfer', () => {
        it('should transfer money successfully', async () => {
            const transferData: TransferRequest = {
                recipientPhone: '0987654321',
                amount: 100,
            };

            const mockResponse = {
                data: {
                    data: {
                        success: true,
                        newBalance: 900,
                        transactionId: 'txn-123',
                    },
                },
            };

            (apiClient.post as any).mockResolvedValue(mockResponse);

            const result = await walletService.transfer(transferData);

            expect(apiClient.post).toHaveBeenCalledWith('/wallet/transfer', transferData);
            expect(result).toEqual({
                success: true,
                newBalance: 900,
                transactionId: 'txn-123',
            });
        });

        it('should handle transfer failure', async () => {
            const transferData: TransferRequest = {
                recipientPhone: '0987654321',
                amount: 100,
            };

            (apiClient.post as any).mockRejectedValue(new Error('Insufficient funds'));

            await expect(walletService.transfer(transferData)).rejects.toThrow('Insufficient funds');
        });
    });

    describe('addFunds', () => {
        it('should add funds successfully', async () => {
            const fundsData: AddFundsRequest = {
                amount: 500,
            };

            const mockResponse = {
                data: {
                    data: {
                        success: true,
                        newBalance: 1500,
                        transactionId: 'txn-456',
                    },
                },
            };

            (apiClient.post as any).mockResolvedValue(mockResponse);

            const result = await walletService.addFunds(fundsData);

            expect(apiClient.post).toHaveBeenCalledWith('/wallet/add-funds', fundsData);
            expect(result).toEqual({
                success: true,
                newBalance: 1500,
                transactionId: 'txn-456',
            });
        });
    });

    describe('getUserProfile', () => {
        it('should return user profile', async () => {
            const mockResponse = {
                data: {
                    data: mockUser,
                },
            };

            (apiClient.get as any).mockResolvedValue(mockResponse);

            const result = await walletService.getUserProfile();

            expect(apiClient.get).toHaveBeenCalledWith('/wallet/profile');
            expect(result).toEqual(mockUser);
        });
    });

    describe('validateTransfer', () => {
        it('should validate correct transfer data', () => {
            const transferData: TransferRequest = {
                recipientPhone: '0987654321',
                amount: 100,
            };

            const result = walletService.validateTransfer(transferData, 1000);

            expect(result.isValid).toBe(true);
            expect(result.errors).toHaveLength(0);
        });

        it('should reject empty recipient phone', () => {
            const transferData: TransferRequest = {
                recipientPhone: '',
                amount: 100,
            };

            const result = walletService.validateTransfer(transferData);

            expect(result.isValid).toBe(false);
            expect(result.errors).toContain('Recipient phone number is required');
        });

        it('should reject invalid phone format', () => {
            const transferData: TransferRequest = {
                recipientPhone: '123',
                amount: 100,
            };

            const result = walletService.validateTransfer(transferData);

            expect(result.isValid).toBe(false);
            expect(result.errors).toContain('Recipient phone number must be at least 10 digits');
        });

        it('should reject zero amount', () => {
            const transferData: TransferRequest = {
                recipientPhone: '0987654321',
                amount: 0,
            };

            const result = walletService.validateTransfer(transferData);

            expect(result.isValid).toBe(false);
            expect(result.errors).toContain('Transfer amount must be greater than 0');
        });

        it('should reject amount exceeding maximum', () => {
            const transferData: TransferRequest = {
                recipientPhone: '0987654321',
                amount: 15000,
            };

            const result = walletService.validateTransfer(transferData);

            expect(result.isValid).toBe(false);
            expect(result.errors).toContain('Maximum transfer amount is $10,000');
        });

        it('should reject amount exceeding balance', () => {
            const transferData: TransferRequest = {
                recipientPhone: '0987654321',
                amount: 1500,
            };

            const result = walletService.validateTransfer(transferData, 1000);

            expect(result.isValid).toBe(false);
            expect(result.errors).toContain('Insufficient funds for this transfer');
        });

        it('should reject amount with more than 2 decimal places', () => {
            const transferData: TransferRequest = {
                recipientPhone: '0987654321',
                amount: 100.123,
            };

            const result = walletService.validateTransfer(transferData);

            expect(result.isValid).toBe(false);
            expect(result.errors).toContain('Amount cannot have more than 2 decimal places');
        });
    });

    describe('validateAddFunds', () => {
        it('should validate correct add funds data', () => {
            const fundsData: AddFundsRequest = {
                amount: 500,
            };

            const result = walletService.validateAddFunds(fundsData);

            expect(result.isValid).toBe(true);
            expect(result.errors).toHaveLength(0);
        });

        it('should reject zero amount', () => {
            const fundsData: AddFundsRequest = {
                amount: 0,
            };

            const result = walletService.validateAddFunds(fundsData);

            expect(result.isValid).toBe(false);
            expect(result.errors).toContain('Amount must be greater than 0');
        });

        it('should reject amount below minimum', () => {
            const fundsData: AddFundsRequest = {
                amount: 0.5,
            };

            const result = walletService.validateAddFunds(fundsData);

            expect(result.isValid).toBe(false);
            expect(result.errors).toContain('Minimum add funds amount is $1');
        });

        it('should reject amount exceeding maximum', () => {
            const fundsData: AddFundsRequest = {
                amount: 15000,
            };

            const result = walletService.validateAddFunds(fundsData);

            expect(result.isValid).toBe(false);
            expect(result.errors).toContain('Maximum add funds amount is $10,000');
        });
    });

    describe('isTransferToSelf', () => {
        it('should detect transfer to self', () => {
            const result = walletService.isTransferToSelf('1234567890', '1234567890');

            expect(result).toBe(true);
        });

        it('should detect transfer to self with different formatting', () => {
            const result = walletService.isTransferToSelf('(123) 456-7890', '1234567890');

            expect(result).toBe(true);
        });

        it('should allow transfer to different number', () => {
            const result = walletService.isTransferToSelf('0987654321', '1234567890');

            expect(result).toBe(false);
        });

        it('should return false when current user phone is not provided', () => {
            const result = walletService.isTransferToSelf('1234567890');

            expect(result).toBe(false);
        });
    });

    describe('formatCurrency', () => {
        it('should format currency correctly', () => {
            const result = walletService.formatCurrency(1234.56);

            expect(result).toBe('$1,234.56');
        });

        it('should format currency with different currency code', () => {
            const result = walletService.formatCurrency(1234.56, 'EUR');

            expect(result).toBe('€1,234.56');
        });

        it('should handle zero amount', () => {
            const result = walletService.formatCurrency(0);

            expect(result).toBe('$0.00');
        });
    });

    describe('parseCurrency', () => {
        it('should parse currency string correctly', () => {
            const result = walletService.parseCurrency('$1,234.56');

            expect(result).toBe(1234.56);
        });

        it('should handle currency string without symbols', () => {
            const result = walletService.parseCurrency('1234.56');

            expect(result).toBe(1234.56);
        });

        it('should return 0 for invalid currency string', () => {
            const result = walletService.parseCurrency('invalid');

            expect(result).toBe(0);
        });
    });

    describe('validatePhoneNumber', () => {
        it('should validate and format correct phone number', () => {
            const result = walletService.validatePhoneNumber('1234567890');

            expect(result.isValid).toBe(true);
            expect(result.formatted).toBe('(123) 456-7890');
        });

        it('should validate phone number with formatting', () => {
            const result = walletService.validatePhoneNumber('(123) 456-7890');

            expect(result.isValid).toBe(true);
            expect(result.formatted).toBe('(123) 456-7890');
        });

        it('should reject short phone number', () => {
            const result = walletService.validatePhoneNumber('123456789');

            expect(result.isValid).toBe(false);
            expect(result.formatted).toBe('123456789');
        });

        it('should handle international numbers', () => {
            const result = walletService.validatePhoneNumber('12345678901');

            expect(result.isValid).toBe(true);
            expect(result.formatted).toBe('12345678901');
        });
    });

    describe('getTransferLimits', () => {
        it('should return transfer limits', () => {
            const result = walletService.getTransferLimits();

            expect(result).toEqual({
                minAmount: 0.01,
                maxAmount: 10000,
                dailyLimit: 25000,
            });
        });
    });

    describe('calculateTransferFee', () => {
        it('should return zero fee', () => {
            const result = walletService.calculateTransferFee(100);

            expect(result).toBe(0);
        });
    });
});