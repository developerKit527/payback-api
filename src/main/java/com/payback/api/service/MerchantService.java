package com.payback.api.service;

import com.payback.api.dto.MerchantDTO;

import java.util.List;

public interface MerchantService {
    
    List<MerchantDTO> getAllMerchants();
    
    void incrementClickCount(Long merchantId);
    
    void initializeSeedData();
}
