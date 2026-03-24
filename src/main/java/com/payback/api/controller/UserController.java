package com.payback.api.controller;

import com.payback.api.dto.UpdateUserRequestDTO;
import com.payback.api.dto.UserDTO;
import com.payback.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @PutMapping("/users/me")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UpdateUserRequestDTO request,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                .body("{\"error\":\"UNAUTHORIZED\",\"message\":\"Authentication required\"}");
        }
        
        Long userId = (Long) authentication.getPrincipal();
        UserDTO updatedUser = userService.updateUserProfile(userId, request.getName());
        
        return ResponseEntity.ok(updatedUser);
    }
}
