package com.payback.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponseDTO {
    
    private Long userId;
    private BigDecimal totalBalance;
    private String upiId;
    private BigDecimal totalEarned;
    private BigDecimal pendingAmount;
    private BigDecimal availableBalance;
    private List<TransactionDTO> transactions;
}
