package com.fintech.wallet.repository;

import com.fintech.wallet.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    // Verifica si la llave enviada en el Header HTTP ya fue procesada anteriormente
    Optional<IdempotencyKey> findByIdempotencyKey(String idempotencyKey);
}