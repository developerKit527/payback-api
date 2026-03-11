package com.payback.api.service.impl;

import com.payback.api.dto.TransactionDTO;
import com.payback.api.dto.WalletResponseDTO;
import com.payback.api.entity.Transaction;
import com.payback.api.entity.TransactionStatus;
import com.payback.api.entity.Wallet;
import com.payback.api.exception.EntityNotFoundException;
import com.payback.api.repository.TransactionRepository;
import com.payback.api.repository.WalletRepository;
import com.payback.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    
    @Override
    public WalletResponseDTO getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet for user " + userId + " not found"));
        
        // Get all transactions for this wallet
        List<Transaction> transactions = transactionRepository.findByWalletOrderByCreatedAtDesc(wallet);
        
        // Calculate balances on-the-fly
        BigDecimal totalEarned = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        BigDecimal availableBalance = BigDecimal.ZERO;
        
        for (Transaction transaction : transactions) {
            // Total earned = sum of all cashback amounts regardless of status
            totalEarned = totalEarned.add(transaction.getCashbackAmount());
            
            // Pending amount = sum of PENDING transactions
            if (transaction.getStatus() == TransactionStatus.PENDING) {
                pendingAmount = pendingAmount.add(transaction.getCashbackAmount());
            }
            
            // Available balance = sum of CONFIRMED transactions
            if (transaction.getStatus() == TransactionStatus.CONFIRMED) {
                availableBalance = availableBalance.add(transaction.getCashbackAmount());
            }
        }
        
        // Convert transactions to DTOs
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(this::convertTransactionToDTO)
                .collect(Collectors.toList());
        
        // Build response DTO
        WalletResponseDTO response = new WalletResponseDTO();
        response.setUserId(wallet.getUserId());
        response.setTotalBalance(wallet.getTotalBalance());
        response.setUpiId(wallet.getUpiId());
        response.setTotalEarned(totalEarned);
        response.setPendingAmount(pendingAmount);
        response.setAvailableBalance(availableBalance);
        response.setTransactions(transactionDTOs);
        
        return response;
    }
    
    @Override
    @Transactional
    public Wallet createWallet(Long userId, String upiId) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setTotalBalance(BigDecimal.ZERO);
        wallet.setUpiId(upiId);
        return walletRepository.save(wallet);
    }
    
    @Override
    @Transactional
    public void initializeSeedData() {
        // Check if wallet for user 1 already exists
        if (walletRepository.findByUserId(1L).isEmpty()) {
            Wallet wallet = new Wallet();
            wallet.setUserId(1L);
            wallet.setTotalBalance(BigDecimal.ZERO);
            wallet.setUpiId("user1@paytm");
            walletRepository.save(wallet);
        }
    }
    
    private TransactionDTO convertTransactionToDTO(Transaction transaction) {
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
