package com.piyush.tradex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String verificationLink = baseUrl + "/api/auth/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("TradEx - Verify Your Email Address");
        message.setText(
                "Hello,\n\n" +
                "Thank you for registering on TradEx!\n\n" +
                "Please click the link below to verify your email address:\n" +
                verificationLink + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not create an account, you can safely ignore this email.\n\n" +
                "Regards,\nThe TradEx Team"
        );

        mailSender.send(message);
    }
}
