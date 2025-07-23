export interface ApiResponse<T> {
    data: T;
    message?: string;
    status: number;
}

export interface ApiError {
    status: number;
    message: string;
    timestamp: string;
    path: string;
}

export interface LoginRequest {
    phoneNumber: string;
    pin: string;
}

export interface RegisterRequest {
    name: string;
    phoneNumber: string;
    pin: string;
}

export interface AuthResponse {
    token: string;
    user: User;
    expiresAt: string;
}

export interface User {
    id: string;
    name: string;
    phoneNumber: string;
    balance: number;
    createdAt: string;
}

export interface TransferRequest {
    recipientPhone: string;
    amount: number;
}

export interface AddFundsRequest {
    amount: number;
}

export interface Transaction {
    id: string;
    senderId: string;
    receiverId: string;
    amount: number;
    type: "TRANSFER" | "DEPOSIT";
    timestamp: string;
    senderName?: string;
    receiverName?: string;
}

export interface TransactionHistory {
    transactions: Transaction[];
    totalPages: number;
    currentPage: number;
    totalCount: number;
}

export interface TransactionFilters {
    page?: number;
    pageSize?: number;
    type?: "TRANSFER" | "DEPOSIT" | "ALL";
    startDate?: string;
    endDate?: string;
    minAmount?: number;
    maxAmount?: number;
}