package com.payback.api.controller;

import com.payback.api.dto.ReferralStatsDTO;
import com.payback.api.entity.User;
import com.payback.api.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/referrals")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @GetMapping("/stats")
    public ResponseEntity<ReferralStatsDTO> getReferralStats(@AuthenticationPrincipal User user) {
        ReferralStatsDTO stats = referralService.getReferralStats(user.getId());
        return ResponseEntity.ok(stats);
    }
}
