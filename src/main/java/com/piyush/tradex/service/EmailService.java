package com.piyush.tradex.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String token);
}
