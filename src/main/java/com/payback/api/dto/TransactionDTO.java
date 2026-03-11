package com.payback.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    
    private Long id;
    private String merchantName;
    private BigDecimal orderAmount;
    private BigDecimal cashbackAmount;
    private String status;
    private LocalDateTime createdAt;
}
