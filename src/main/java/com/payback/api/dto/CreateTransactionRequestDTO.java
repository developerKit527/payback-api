package com.payback.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransactionRequestDTO {
    private Long merchantId;
    private BigDecimal orderAmount;
}
