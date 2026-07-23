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

        // 2. Buscar billetera origen
        Wallet sourceWallet = walletRepository.findById(dto.sourceWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Source wallet not found"));

        // 3. Buscar billetera destino
        Wallet targetWallet = walletRepository.findById(dto.targetWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Target wallet not found"));

        // 4. Verificar saldo suficiente
        if (sourceWallet.getBalance().compareTo(dto.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds in source wallet");
        }

        // 5. Descontar dinero del emisor y acreditar al receptor
        sourceWallet.setBalance(sourceWallet.getBalance().subtract(dto.amount()));
        targetWallet.setBalance(targetWallet.getBalance().add(dto.amount()));

        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);

        // 6. Registrar el comprobante de la transacción
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