package com.payback.api.service;

import com.payback.api.dto.MerchantDTO;
import com.payback.api.dto.MerchantDetailDTO;

import java.util.List;

public interface MerchantService {
    
    List<MerchantDTO> getAllMerchants();
    
    MerchantDetailDTO getMerchantById(Long merchantId);
    
    String incrementClickCount(Long merchantId);
    
    void initializeSeedData();
}
