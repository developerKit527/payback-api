package com.payback.api.controller;

import com.payback.api.dto.WalletResponseDTO;
import com.payback.api.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponseDTO> getWalletByUserId(@PathVariable Long userId) {
        WalletResponseDTO wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }
}
