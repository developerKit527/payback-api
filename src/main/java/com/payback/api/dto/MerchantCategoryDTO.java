package com.payback.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantCategoryDTO {
    private Long id;
    private String name;
    private String icon;
    private String affiliateUrl;
    private Double cashbackRate;
    private Integer displayOrder;
}
