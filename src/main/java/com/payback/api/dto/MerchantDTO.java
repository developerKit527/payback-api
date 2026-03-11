package com.payback.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantDTO {
    
    private Long id;
    private String name;
    private String logoUrl;
    private Double cashbackRate;
    private String manualTrackingUrl;
    private Long clickCount;
}
