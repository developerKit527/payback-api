package com.payback.api.controller;

import com.payback.api.dto.MerchantDTO;
import com.payback.api.service.MerchantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping
    public ResponseEntity<List<MerchantDTO>> getAllMerchants() {
        List<MerchantDTO> merchants = merchantService.getAllMerchants();
        return ResponseEntity.ok(merchants);
    }

    @GetMapping("/{id}/click")
    public ResponseEntity<Void> incrementClickCount(@PathVariable Long id) {
        merchantService.incrementClickCount(id);
        return ResponseEntity.ok().build();
    }
}
