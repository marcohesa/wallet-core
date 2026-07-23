package com.fintech.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequestDto(
    @NotNull(message = "Source wallet ID is required")
    Long sourceWalletId,

    @NotNull(message = "Target wallet ID is required")
    Long targetWalletId,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    BigDecimal amount
) {}