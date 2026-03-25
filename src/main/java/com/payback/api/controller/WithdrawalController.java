package com.payback.api.controller;

import com.payback.api.dto.WithdrawalDTO;
import com.payback.api.dto.WithdrawalRequestDTO;
import com.payback.api.service.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping("/withdrawals")
    public ResponseEntity<WithdrawalDTO> createWithdrawalRequest(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody WithdrawalRequestDTO requestDTO) {
        WithdrawalDTO withdrawal = withdrawalService.createWithdrawalRequest(userId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(withdrawal);
    }

    @GetMapping("/withdrawals/history")
    public ResponseEntity<List<WithdrawalDTO>> getWithdrawalHistory(
            @AuthenticationPrincipal Long userId) {
        List<WithdrawalDTO> history = withdrawalService.getWithdrawalHistory(userId);
        return ResponseEntity.ok(history);
    }

    @PutMapping("/admin/withdrawals/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WithdrawalDTO> approveWithdrawal(@PathVariable Long id) {
        WithdrawalDTO withdrawal = withdrawalService.approveWithdrawal(id);
        return ResponseEntity.ok(withdrawal);
    }

    @PutMapping("/admin/withdrawals/{id}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WithdrawalDTO> markAsPaid(@PathVariable Long id) {
        WithdrawalDTO withdrawal = withdrawalService.markAsPaid(id);
        return ResponseEntity.ok(withdrawal);
    }
}
