package com.fintech.wallet.service;

import com.fintech.wallet.dto.TransactionResponseDto;
import com.fintech.wallet.dto.TransferRequestDto;
import com.fintech.wallet.entity.Transaction;
import com.fintech.wallet.entity.Transaction.TransactionStatus;
import com.fintech.wallet.entity.Transaction.TransactionType;
import com.fintech.wallet.entity.Wallet;
import com.fintech.wallet.repository.TransactionRepository;
import com.fintech.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TransactionResponseDto transfer(TransferRequestDto dto) {
        // 1. Validar que las billeteras no sean las mismas
        if (dto.sourceWalletId().equals(dto.targetWalletId())) {
            throw new IllegalArgumentException("Cannot transfer funds to the same wallet");
        }

        // 2 y 3. Garantizar un orden consistente de bloqueo para evitar Deadlocks
        Long firstId = dto.sourceWalletId() < dto.targetWalletId() ? dto.sourceWalletId() : dto.targetWalletId();
        Long secondId = dto.sourceWalletId() < dto.targetWalletId() ? dto.targetWalletId() : dto.sourceWalletId();

        Wallet firstWallet = walletRepository.findByIdWithLock(firstId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + firstId));
        Wallet secondWallet = walletRepository.findByIdWithLock(secondId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + secondId));

        // Re-asignar a source y target correspondientes
        Wallet sourceWallet = dto.sourceWalletId().equals(firstWallet.getId()) ? firstWallet : secondWallet;
        Wallet targetWallet = dto.targetWalletId().equals(firstWallet.getId()) ? firstWallet : secondWallet;

        // 4. Verificar saldo suficiente
        if (sourceWallet.getBalance().compareTo(dto.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds in source wallet");
        }

        // 5. Descontar y acreditar fondos
        sourceWallet.setBalance(sourceWallet.getBalance().subtract(dto.amount()));
        targetWallet.setBalance(targetWallet.getBalance().add(dto.amount()));

        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);

        // 6. Registrar transacción en bitácora
        Transaction transaction = Transaction.builder()
                .sourceWallet(sourceWallet)
                .targetWallet(targetWallet)
                .amount(dto.amount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .referenceId(UUID.randomUUID().toString())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return new TransactionResponseDto(
                savedTransaction.getId(),
                sourceWallet.getId(),
                targetWallet.getId(),
                savedTransaction.getAmount(),
                savedTransaction.getType(),
                savedTransaction.getStatus(),
                savedTransaction.getReferenceId(),
                savedTransaction.getCreatedAt()
        );
    }
}