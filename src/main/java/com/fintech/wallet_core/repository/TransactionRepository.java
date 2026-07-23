package com.fintech.wallet.repository;

import com.fintech.wallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Busca por la referencia bancaria o de seguimiento
    Optional<Transaction> findByReferenceId(String referenceId);

    // Obtiene el historial de envíos o recepciones de una billetera
    List<Transaction> findBySourceWalletIdOrTargetWalletIdOrderByCreatedAtDesc(Long sourceId, Long targetId);
}