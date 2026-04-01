package com.piyush.tradex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.piyush.tradex.dto.AddMoneyRequestDTO;
import com.piyush.tradex.dto.DeductMoneyRequestDTO;
import com.piyush.tradex.dto.WalletResponseDTO;
import com.piyush.tradex.service.WalletService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/wallet")
@Validated
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/add")
    public ResponseEntity<WalletResponseDTO> addMoney(@Valid @RequestBody AddMoneyRequestDTO request) {
        String email = getAuthenticatedEmail();
        WalletResponseDTO response = walletService.addMoney(email, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deduct")
    public ResponseEntity<WalletResponseDTO> deductMoney(@Valid @RequestBody DeductMoneyRequestDTO request) {
        String email = getAuthenticatedEmail();
        WalletResponseDTO response = walletService.deductMoney(email, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
    public ResponseEntity<WalletResponseDTO> getBalance() {
        String email = getAuthenticatedEmail();
        WalletResponseDTO response = walletService.getBalance(email);
        return ResponseEntity.ok(response);
    }

    private String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) authentication.getPrincipal();
    }
}
