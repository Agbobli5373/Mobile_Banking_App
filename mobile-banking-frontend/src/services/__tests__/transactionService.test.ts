import { describe, it, expect, vi, beforeEach } from 'vitest';
import { transactionService } from '../transactionService';
import apiClient from '../apiClient';
import type { Transaction, TransactionHistory, TransactionFilters } from '../../types/api';

// Mock the API client
vi.mock('../apiClient', () => ({
  default: {
    get: vi.fn(),
  },
}));

describe('TransactionService', () => {
  const mockTransactions: Transaction[] = [
    {
      id: 'txn-1',
      senderId: '123',
      receiverId: '456',
      amount: 100,
      type: 'TRANSFER',
      timestamp: '2023-01-01T10:00:00Z',
      senderName: 'John Doe',
      receiverName: 'Jane Smith',
    },
    {
      id: 'txn-2',
      senderId: '456',
      receiverId: '123',
      amount: 50,
      type: 'TRANSFER',
      timestamp: '2023-01-02T11:00:00Z',
      senderName: 'Jane Smith',
      receiverName: 'John Doe',
    },
  ];

  const mockTransactionHistory: TransactionHistory = {
    transactions: mockTransactions,
    totalPages: 1,
    currentPage: 0,
    totalCount: 2,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getTransactions', () => {
    it('should get transactions with default filters', async () => {
      const mockResponse = {
        data: {
          data: mockTransactionHistory,
        },
      };

      (apiClient.get as any).mockResolvedValue(mockResponse);

      const result = await transactionService.getTransactions();

      expect(apiClient.get).toHaveBeenCalledWith('/transactions');
      expect(result).toEqual(mockTransactionHistory);
    });

    it('should get transactions with filters', async () => {
      const filters: TransactionFilters = {
        page: 1,
        pageSize: 10,
        type: 'TRANSFER',
        startDate: '2023-01-01',
        endDate: '2023-01-31',
        minAmount: 50,
        maxAmount: 500,
      };

      const mockResponse = {
        data: {
          data: mockTransactionHistory,
        },
      };

      (apiClient.get as any).mockResolvedValue(mockResponse);

      const result = await transactionService.getTransactions(filters);

      expect(apiClient.get).toHaveBeenCalledWith(
        '/transactions?page=1&pageSize=10&type=TRANSFER&startDate=2023-01-01&endDate=2023-01-31&minAmount=50&maxAmount=500'
      );
      expect(result).toEqual(mockTransactionHistory);
    });
  });

  describe('getTransaction', () => {
    it('should get single transaction by ID', async () => {
      const mockResponse = {
        data: {
          data: mockTransactions[0],
        },
      };

      (apiClient.get as any).mockResolvedValue(mockResponse);

      const result = await transactionService.getTransaction('txn-1');

      expect(apiClient.get).toHaveBeenCalledWith('/transactions/txn-1');
      expect(result).toEqual(mockTransactions[0]);
    });
  });

  describe('validateFilters', () => {
    it('should validate correct filters', () => {
      const filters: TransactionFilters = {
        page: 1,
        pageSize: 20,
        type: 'TRANSFER',
        startDate: '2023-01-01',
        endDate: '2023-01-31',
        minAmount: 10,
        maxAmount: 1000,
      };

      const result = transactionService.validateFilters(filters);

      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    it('should reject negative page number', () => {
      const filters: TransactionFilters = {
        page: -1,
      };

      const result = transactionService.validateFilters(filters);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Page number must be 0 or greater');
    });
  });

  describe('formatTransaction', () => {
    it('should format outgoing transfer', () => {
      const transaction = mockTransactions[0];
      const result = transactionService.formatTransaction(transaction, '123');

      expect(result.displayName).toBe('Jane Smith');
      expect(result.isIncoming).toBe(false);
      expect(result.transactionType).toBe('sent');
    });

    it('should format incoming transfer', () => {
      const transaction = mockTransactions[1];
      const result = transactionService.formatTransaction(transaction, '123');

      expect(result.displayName).toBe('Jane Smith');
      expect(result.isIncoming).toBe(true);
      expect(result.transactionType).toBe('received');
    });
  });

  describe('formatCurrency', () => {
    it('should format currency correctly', () => {
      const result = transactionService.formatCurrency(1234.56);

      expect(result).toBe('$1,234.56');
    });
  });
});