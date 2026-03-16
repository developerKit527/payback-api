package com.payback.api.controller;

import com.payback.api.dto.MerchantDTO;
import com.payback.api.service.MerchantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/merchants")
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
    public ResponseEntity<Map<String, String>> incrementClickCount(@PathVariable Long id) {
        String url = merchantService.incrementClickCount(id);
        Map<String, String> response = new HashMap<>();
        if (url == null || url.isEmpty()) {
            response.put("error", "Merchant URL not configured");
            return ResponseEntity.status(404).body(response);
        }
        response.put("url", url);
        return ResponseEntity.ok(response);
    }
}
