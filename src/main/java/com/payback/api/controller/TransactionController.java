package com.payback.api.controller;

import com.payback.api.dto.CreateTransactionRequestDTO;
import com.payback.api.dto.TransactionDTO;
import com.payback.api.dto.UpdateTransactionStatusRequestDTO;
import com.payback.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(
            @RequestBody CreateTransactionRequestDTO request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        TransactionDTO dto = transactionService.createTransactionForUser(
                userId, request.getMerchantId(), request.getOrderAmount());
        return ResponseEntity.status(201).body(dto);
    }

    @GetMapping("/me")
    public ResponseEntity<List<TransactionDTO>> getMyTransactions(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(transactionService.getTransactionsByUserId(userId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TransactionDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateTransactionStatusRequestDTO request) {
        return ResponseEntity.ok(transactionService.updateTransactionStatusAndReturn(id, request.getStatus()));
    }
}
