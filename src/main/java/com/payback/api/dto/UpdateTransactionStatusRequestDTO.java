package com.payback.api.dto;

import com.payback.api.entity.TransactionStatus;
import lombok.Data;

@Data
public class UpdateTransactionStatusRequestDTO {
    private TransactionStatus status;
}
