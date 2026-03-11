package com.payback.api.service.impl;

import com.payback.api.dto.TransactionDTO;
import com.payback.api.entity.Transaction;
import com.payback.api.entity.TransactionStatus;
import com.payback.api.entity.Wallet;
import com.payback.api.exception.EntityNotFoundException;
import com.payback.api.repository.TransactionRepository;
import com.payback.api.repository.WalletRepository;
import com.payback.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    
    @Override
    @Transactional
    public Transaction createTransaction(Long walletId, String merchantName, 
                                        BigDecimal orderAmount, BigDecimal cashbackAmount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet with id " + walletId + " not found"));
        
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setMerchantName(merchantName);
        transaction.setOrderAmount(orderAmount);
        transaction.setCashbackAmount(cashbackAmount);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());
        
        return transactionRepository.save(transaction);
    }
    
    @Override
    @Transactional
    public void updateTransactionStatus(Long transactionId, TransactionStatus newStatus) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction with id " + transactionId + " not found"));
        
        TransactionStatus oldStatus = transaction.getStatus();
        transaction.setStatus(newStatus);
        transactionRepository.save(transaction);
        
        // Update wallet balance if status changed to CONFIRMED
        if (newStatus == TransactionStatus.CONFIRMED && oldStatus != TransactionStatus.CONFIRMED) {
            Wallet wallet = transaction.getWallet();
            wallet.setTotalBalance(wallet.getTotalBalance().add(transaction.getCashbackAmount()));
            walletRepository.save(wallet);
        }
    }
    
    @Override
    public List<TransactionDTO> getTransactionsByWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet with id " + walletId + " not found"));
        
        return transactionRepository.findByWalletOrderByCreatedAtDesc(wallet)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void initializeSeedData() {
        // Find wallet for user 1
        Wallet wallet = walletRepository.findByUserId(1L).orElse(null);
        
        if (wallet != null && transactionRepository.findByWalletOrderByCreatedAtDesc(wallet).isEmpty()) {
            // Create first transaction - Flipkart
            Transaction transaction1 = new Transaction();
            transaction1.setWallet(wallet);
            transaction1.setMerchantName("Flipkart");
            transaction1.setOrderAmount(new BigDecimal("2000.00"));
            transaction1.setCashbackAmount(new BigDecimal("200.00"));
            transaction1.setStatus(TransactionStatus.PENDING);
            transaction1.setCreatedAt(LocalDateTime.now());
            
            // Create second transaction - Myntra
            Transaction transaction2 = new Transaction();
            transaction2.setWallet(wallet);
            transaction2.setMerchantName("Myntra");
            transaction2.setOrderAmount(new BigDecimal("1500.00"));
            transaction2.setCashbackAmount(new BigDecimal("127.50"));
            transaction2.setStatus(TransactionStatus.PENDING);
            transaction2.setCreatedAt(LocalDateTime.now());
            
            transactionRepository.save(transaction1);
            transactionRepository.save(transaction2);
        }
    }
    
    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setMerchantName(transaction.getMerchantName());
        dto.setOrderAmount(transaction.getOrderAmount());
        dto.setCashbackAmount(transaction.getCashbackAmount());
        dto.setStatus(transaction.getStatus().name());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
}
