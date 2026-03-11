package com.payback.api.service;

import com.payback.api.dto.WalletResponseDTO;
import com.payback.api.entity.Wallet;

public interface WalletService {
    
    WalletResponseDTO getWalletByUserId(Long userId);
    
    Wallet createWallet(Long userId, String upiId);
    
    void initializeSeedData();
}
