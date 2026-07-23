package com.fintech.wallet.dto;

import java.math.BigDecimal;

public record WalletResponseDto(
    Long id,
    BigDecimal balance,
    String currency
) {}