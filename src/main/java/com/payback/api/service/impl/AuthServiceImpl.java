package com.payback.api.service.impl;

import com.payback.api.dto.AuthResponseDTO;
import com.payback.api.dto.LoginRequestDTO;
import com.payback.api.dto.RegisterRequestDTO;
import com.payback.api.dto.UserDTO;
import com.payback.api.entity.User;
import com.payback.api.repository.UserRepository;
import com.payback.api.service.AuthService;
import com.payback.api.service.JwtService;
import com.payback.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletService walletService;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User(null, request.getName(), request.getEmail(),
                passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);
        walletService.createWallet(user.getId(), null);
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
