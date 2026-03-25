package com.payback.api.dto;

import com.payback.api.entity.WithdrawalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalDTO {

    private Long id;
    private String upiId;
    private BigDecimal amount;
    private WithdrawalStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
}
