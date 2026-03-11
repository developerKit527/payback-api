package com.payback.api.entity;

public enum TransactionStatus {
    PENDING,    // Tracked by the network but not yet payable
    CONFIRMED,  // Cashback received from the merchant; ready for withdrawal
    REJECTED    // Order cancelled or returned by the user
}
