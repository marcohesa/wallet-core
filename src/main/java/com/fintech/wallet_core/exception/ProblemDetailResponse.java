package com.fintech.wallet.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ProblemDetailResponse(
    String title,
    int status,
    String detail,
    String instance,
    OffsetDateTime timestamp,
    Map<String, String> errors // Para errores de validación de campos
) {}