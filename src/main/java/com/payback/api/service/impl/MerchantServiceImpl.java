package com.payback.api.service.impl;

import com.payback.api.dto.MerchantCategoryDTO;
import com.payback.api.dto.MerchantDTO;
import com.payback.api.dto.MerchantDetailDTO;
import com.payback.api.dto.MerchantOfferDTO;
import com.payback.api.entity.Merchant;
import com.payback.api.exception.EntityNotFoundException;
import com.payback.api.repository.MerchantCategoryRepository;
import com.payback.api.repository.MerchantOfferRepository;
import com.payback.api.repository.MerchantRepository;
import com.payback.api.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {
    
    private final MerchantRepository merchantRepository;
    private final MerchantCategoryRepository merchantCategoryRepository;
    private final MerchantOfferRepository merchantOfferRepository;
    
    @Override
    public List<MerchantDTO> getAllMerchants() {
        return merchantRepository.findAllByOrderByClickCountDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MerchantDetailDTO getMerchantById(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException("Merchant with id " + merchantId + " not found"));

        List<MerchantCategoryDTO> categories = merchantCategoryRepository
                .findByMerchantIdOrderByDisplayOrderAsc(merchantId)
                .stream()
                .map(c -> new MerchantCategoryDTO(c.getId(), c.getName(), c.getIcon(),
                        c.getAffiliateUrl(), c.getCashbackRate(), c.getDisplayOrder()))
                .collect(Collectors.toList());

        List<MerchantOfferDTO> offers = merchantOfferRepository
                .findByMerchantIdAndIsActiveTrue(merchantId)
                .stream()
                .map(o -> new MerchantOfferDTO(o.getId(), o.getTitle(), o.getDescription(),
                        o.getDiscountText(), o.getAffiliateUrl(), o.getIsActive()))
                .collect(Collectors.toList());

        return new MerchantDetailDTO(merchant.getId(), merchant.getName(), merchant.getLogoUrl(),
                merchant.getCashbackRate(), merchant.getManualTrackingUrl(),
                merchant.getClickCount(), categories, offers);
    }
    
    @Override
    @Transactional
    public String incrementClickCount(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException("Merchant with id " + merchantId + " not found"));
        
        merchant.setClickCount(merchant.getClickCount() + 1);
        merchantRepository.save(merchant);
        return merchant.getManualTrackingUrl();
    }
    
    @Override
    @Transactional
    public void initializeSeedData() {
        // Check if merchants already exist
        if (merchantRepository.count() == 0) {
            // Create Flipkart
            Merchant flipkart = new Merchant();
            flipkart.setName("Flipkart");
            flipkart.setLogoUrl("https://example.com/flipkart-logo.png");
            flipkart.setCashbackRate(10.0);
            flipkart.setManualTrackingUrl("https://tracking.example.com/flipkart");
            flipkart.setClickCount(0L);
            
            // Create Myntra
            Merchant myntra = new Merchant();
            myntra.setName("Myntra");
            myntra.setLogoUrl("https://example.com/myntra-logo.png");
            myntra.setCashbackRate(8.5);
            myntra.setManualTrackingUrl("https://tracking.example.com/myntra");
            myntra.setClickCount(0L);
            
            // Create Ajio
            Merchant ajio = new Merchant();
            ajio.setName("Ajio");
            ajio.setLogoUrl("https://example.com/ajio-logo.png");
            ajio.setCashbackRate(7.0);
            ajio.setManualTrackingUrl("https://tracking.example.com/ajio");
            ajio.setClickCount(0L);
            
            merchantRepository.save(flipkart);
            merchantRepository.save(myntra);
            merchantRepository.save(ajio);
        }
    }
    
    private MerchantDTO convertToDTO(Merchant merchant) {
        MerchantDTO dto = new MerchantDTO();
        dto.setId(merchant.getId());
        dto.setName(merchant.getName());
        dto.setLogoUrl(merchant.getLogoUrl());
        dto.setCashbackRate(merchant.getCashbackRate());
        dto.setManualTrackingUrl(merchant.getManualTrackingUrl());
        dto.setClickCount(merchant.getClickCount());
        return dto;
    }
}
