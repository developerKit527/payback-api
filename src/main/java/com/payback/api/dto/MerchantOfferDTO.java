package com.payback.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantOfferDTO {
    private Long id;
    private String title;
    private String description;
    private String discountText;
    private String affiliateUrl;
    private Boolean isActive;
}
