package com.payback.api.service.impl;

import com.payback.api.dto.AuthResponseDTO;
import com.payback.api.dto.LoginRequestDTO;
import com.payback.api.dto.RegisterRequestDTO;
import com.payback.api.dto.UserDTO;
import com.payback.api.entity.User;
import com.payback.api.repository.UserRepository;
import com.payback.api.service.AuthService;
import com.payback.api.service.JwtService;
import com.payback.api.service.ReferralService;
import com.payback.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletService walletService;
    private final ReferralService referralService;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Generate unique referral code for new user
        String referralCode = referralService.generateReferralCode();
        
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setReferralCode(referralCode);
        user.setCashbackBalance(BigDecimal.ZERO);
        
        user = userRepository.save(user);
        walletService.createWallet(user.getId(), null);
        
        // Handle referral code if provided
        if (request.getReferralCode() != null && !request.getReferralCode().trim().isEmpty()) {
            referralService.createReferral(request.getReferralCode(), user);
        }
        
        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token, new UserDTO(user.getId(), user.getName(), user.getEmail()));
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPasswordHash()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token, new UserDTO(user.getId(), user.getName(), user.getEmail()));
    }
}
