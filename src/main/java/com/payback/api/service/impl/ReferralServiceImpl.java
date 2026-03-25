package com.payback.api.service.impl;

import com.payback.api.dto.ReferralStatsDTO;
import com.payback.api.entity.Referral;
import com.payback.api.entity.User;
import com.payback.api.repository.ReferralRepository;
import com.payback.api.repository.UserRepository;
import com.payback.api.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferralServiceImpl implements ReferralService {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;

    @Value("${referral.bonus.amount:50.00}")
    private BigDecimal bonusAmount;

    @Override
    public String generateReferralCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        
        // Check uniqueness
        String generatedCode = code.toString();
        if (userRepository.findByReferralCode(generatedCode).isPresent()) {
            // Retry if collision (very rare with 8 alphanumeric chars)
            return generateReferralCode();
        }
        
        return generatedCode;
    }

    @Override
    @Transactional
    public void createReferral(String referralCode, User newUser) {
        // Validate referral code exists
        User referrer = userRepository.findByReferralCode(referralCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid referral code"));

        // Prevent self-referral
        if (referrer.getId().equals(newUser.getId())) {
            throw new IllegalArgumentException("Cannot use your own referral code");
        }

        // Check if user already has a referral (prevent duplicate)
        if (referralRepository.findByReferrerId(newUser.getId()).stream()
                .anyMatch(r -> r.getReferred().getId().equals(newUser.getId()))) {
            throw new IllegalArgumentException("User already registered with a referral code");
        }

        // Create referral relationship
        Referral referral = new Referral();
        referral.setReferrer(referrer);
        referral.setReferred(newUser);
        referral.setBonusCashback(bonusAmount);
        referral.setCreatedAt(LocalDateTime.now());
        referralRepository.save(referral);

        // Award bonus cashback to both users
        referrer.setCashbackBalance(referrer.getCashbackBalance().add(bonusAmount));
        newUser.setCashbackBalance(newUser.getCashbackBalance().add(bonusAmount));
        userRepository.save(referrer);
        userRepository.save(newUser);
    }

    @Override
    public ReferralStatsDTO getReferralStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Referral> referrals = referralRepository.findByReferrerId(userId);
        
        int totalReferrals = referrals.size();
        BigDecimal totalBonus = referrals.stream()
                .map(Referral::getBonusCashback)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReferralStatsDTO(user.getReferralCode(), totalReferrals, totalBonus);
    }
}
