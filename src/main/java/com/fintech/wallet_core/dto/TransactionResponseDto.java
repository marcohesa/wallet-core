package com.fintech.wallet.dto;

import com.fintech.wallet.entity.Transaction.TransactionStatus;
import com.fintech.wallet.entity.Transaction.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionResponseDto(
    Long id,
    Long sourceWalletId,
    Long targetWalletId,
    BigDecimal amount,
    TransactionType type,
    TransactionStatus status,
    String referenceId,
    OffsetDateTime createdAt
) {}