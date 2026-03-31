package com.piyush.tradex.dto;

public class WalletResponseDTO {

    private long walletId;
    private double balance;
    private String message;

    public WalletResponseDTO() {}

    public WalletResponseDTO(long walletId, double balance, String message) {
        this.walletId = walletId;
        this.balance = balance;
        this.message = message;
    }

    public long getWalletId() { return walletId; }
    public void setWalletId(long walletId) { this.walletId = walletId; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
