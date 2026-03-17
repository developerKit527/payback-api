package com.payback.api.service;

import com.payback.api.dto.AuthResponseDTO;
import com.payback.api.dto.LoginRequestDTO;
import com.payback.api.dto.RegisterRequestDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
}
