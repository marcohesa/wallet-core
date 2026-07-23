package com.fintech.wallet.service;

import com.fintech.wallet.dto.CreateUserDto;
import com.fintech.wallet.dto.UserResponseDto;
import com.fintech.wallet.dto.WalletResponseDto;
import com.fintech.wallet.entity.User;
import com.fintech.wallet.entity.Wallet;
import com.fintech.wallet.repository.UserRepository;
import com.fintech.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public UserResponseDto registerUser(CreateUserDto dto) {
        // 1. Validar que el email no exista
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // 2. Crear y guardar Usuario
        User user = User.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .email(dto.email())
                .build();
        User savedUser = userRepository.save(user);

        // 3. Crear y guardar Billetera inicial ($0.00 USD)
        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .build();
        Wallet savedWallet = walletRepository.save(wallet);

        // 4. Retornar DTO de respuesta
        WalletResponseDto walletDto = new WalletResponseDto(
                savedWallet.getId(),
                savedWallet.getBalance(),
                savedWallet.getCurrency()
        );

        return new UserResponseDto(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                walletDto,
                savedUser.getCreatedAt()
        );
    }
}