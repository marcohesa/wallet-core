package com.fintech.wallet.controller;

import com.fintech.wallet.dto.TransactionResponseDto;
import com.fintech.wallet.dto.TransferRequestDto;
import com.fintech.wallet.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransactionResponseDto> transfer(@Valid @RequestBody TransferRequestDto dto) {
        TransactionResponseDto response = transferService.transfer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}