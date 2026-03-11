package com.payback.api.repository;

import com.payback.api.entity.Transaction;
import com.payback.api.entity.TransactionStatus;
import com.payback.api.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByWalletOrderByCreatedAtDesc(Wallet wallet);
    
    List<Transaction> findByWalletAndStatus(Wallet wallet, TransactionStatus status);
}
