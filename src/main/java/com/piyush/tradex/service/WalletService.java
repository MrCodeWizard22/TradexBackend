package com.piyush.tradex.service;

import com.piyush.tradex.dto.AddMoneyRequestDTO;
import com.piyush.tradex.dto.WalletResponseDTO;

public interface WalletService {
    WalletResponseDTO addMoney(String email, AddMoneyRequestDTO request);
    WalletResponseDTO getBalance(String email);
}
