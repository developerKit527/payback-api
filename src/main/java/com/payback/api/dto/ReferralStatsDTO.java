package com.payback.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralStatsDTO {
    private String referralCode;
    private Integer totalReferrals;
    private BigDecimal totalBonusCashback;
}
