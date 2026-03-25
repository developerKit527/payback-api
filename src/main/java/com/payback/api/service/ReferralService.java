package com.payback.api.service;

import com.payback.api.dto.ReferralStatsDTO;
import com.payback.api.entity.User;

public interface ReferralService {
    String generateReferralCode();
    void createReferral(String referralCode, User newUser);
    ReferralStatsDTO getReferralStats(Long userId);
}
