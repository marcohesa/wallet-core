package com.fintech.wallet.dto;

import java.time.OffsetDateTime;

public record UserResponseDto(
    Long id,
    String firstName,
    String lastName,
    String email,
    WalletResponseDto wallet,
    OffsetDateTime createdAt
) {}