package com.payback.api.service;

import com.payback.api.dto.TransactionDTO;
import com.payback.api.entity.Transaction;
import com.payback.api.entity.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    
    Transaction createTransaction(Long walletId, String merchantName, 
                                  BigDecimal orderAmount, BigDecimal cashbackAmount);
    
    TransactionDTO createTransactionForUser(Long userId, Long merchantId, BigDecimal orderAmount);
    
    void updateTransactionStatus(Long transactionId, TransactionStatus newStatus);
    
    List<TransactionDTO> getTransactionsByWallet(Long walletId);
    
    void initializeSeedData();
}
