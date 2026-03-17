package com.payback.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantDetailDTO {
    private Long id;
    private String name;
    private String logoUrl;
    private Double cashbackRate;
    private String manualTrackingUrl;
    private Long clickCount;
    private List<MerchantCategoryDTO> categories;
    private List<MerchantOfferDTO> offers;
}
