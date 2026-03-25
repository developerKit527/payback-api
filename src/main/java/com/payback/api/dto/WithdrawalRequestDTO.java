package com.payback.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestDTO {

    @NotBlank(message = "UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z]+$", message = "Invalid UPI ID format. Expected: username@bankname")
    private String upiId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.0", message = "Minimum withdrawal amount is 100 rupees")
    private BigDecimal amount;
}
