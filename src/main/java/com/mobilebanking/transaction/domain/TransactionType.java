package com.mobilebanking.transaction.domain;

/**
 * TransactionType enum representing different types of financial transactions
 * in the mobile banking system.
 */
public enum TransactionType {
    /**
     * Money transfer between two users
     */
    TRANSFER,

    /**
     * Fund deposit/addition to a user's wallet
     */
    DEPOSIT
}