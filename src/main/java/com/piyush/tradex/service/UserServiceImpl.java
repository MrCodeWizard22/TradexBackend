package com.piyush.tradex.service;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.piyush.tradex.dto.LoginRequestDTO;
import com.piyush.tradex.dto.LoginResponseDTO;
import com.piyush.tradex.dto.RegisterRequestDTO;
import com.piyush.tradex.dto.RegisterResponseDTO;
import com.piyush.tradex.enitity.User;
import com.piyush.tradex.enitity.VerificationToken;
import com.piyush.tradex.enitity.Wallet;
import com.piyush.tradex.enums.Role;
import com.piyush.tradex.repository.UserRepository;
import com.piyush.tradex.repository.VerificationTokenRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtService jwtService;

    @Override
    public RegisterResponseDTO registerUser(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        Wallet wallet = new Wallet();
        wallet.setBalance(0.0);

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setWallet(wallet);
        user.setVerified(false); 

        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, savedUser);
        tokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), token);

        RegisterResponseDTO response = new RegisterResponseDTO();
        response.setUserId(savedUser.getUserId());
        response.setFirstName(savedUser.getFirstName());
        response.setLastName(savedUser.getLastName());
        response.setEmail(savedUser.getEmail());
        response.setMessage("Registration successful! Please check your email to verify your account.");

        return response;
    }

    @Override
    @Transactional
    public String verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or already used verification token."));

        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            throw new RuntimeException("Verification token has expired. Please register again.");
        }

        User user = verificationToken.getUser();
        user.setVerified(true);
        user.setUpdatedAt(new Date());
        userRepository.save(user);

        tokenRepository.delete(verificationToken);

        return "Email verified successfully! You can now log in.";
    }

    @Override
    public LoginResponseDTO loginUser(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            throw new RuntimeException("No account found with email: " + request.getEmail());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password.");
        }

        if (!user.isVerified()) {
            throw new RuntimeException(
                    "Email not verified. Please check your inbox and verify your email before logging in.");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().toString());
        return new LoginResponseDTO(token, "Login successful!");
    }
}
