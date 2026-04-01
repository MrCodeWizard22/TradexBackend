package com.piyush.tradex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.piyush.tradex.dto.AddMoneyRequestDTO;
import com.piyush.tradex.dto.DeductMoneyRequestDTO;
import com.piyush.tradex.dto.WalletResponseDTO;
import com.piyush.tradex.enitity.User;
import com.piyush.tradex.enitity.Wallet;
import com.piyush.tradex.repository.UserRepository;
import com.piyush.tradex.repository.WalletRepository;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Override
    @Transactional
    public WalletResponseDTO addMoney(String email, AddMoneyRequestDTO request) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new RuntimeException("Wallet not found for user: " + email);
        }

        wallet.setBalance(wallet.getBalance() + request.getAmount());
        walletRepository.save(wallet);

        return new WalletResponseDTO(
                wallet.getWalletId(),
                wallet.getBalance(),
                "₹" + request.getAmount() + " added successfully. New balance: ₹" + wallet.getBalance()
        );
    }

    @Override
    @Transactional
    public WalletResponseDTO deductMoney(String email, DeductMoneyRequestDTO request) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new RuntimeException("Wallet not found for user: " + email);
        }

        if (wallet.getBalance() < request.getAmount()) {
            throw new RuntimeException("Insufficient balance. Available: ₹" + wallet.getBalance());
        }

        wallet.setBalance(wallet.getBalance() - request.getAmount());
        walletRepository.save(wallet);

        return new WalletResponseDTO(
                wallet.getWalletId(),
                wallet.getBalance(),
                "₹" + request.getAmount() + " deducted successfully. New balance: ₹" + wallet.getBalance()
        );
    }

    @Override
    public WalletResponseDTO getBalance(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new RuntimeException("Wallet not found for user: " + email);
        }

        return new WalletResponseDTO(
                wallet.getWalletId(),
                wallet.getBalance(),
                "Current balance: ₹" + wallet.getBalance()
        );
    }
}
