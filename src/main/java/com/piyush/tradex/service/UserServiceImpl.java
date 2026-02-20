package com.piyush.tradex.service;

import java.util.Date;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.piyush.tradex.dto.RegisterRequestDTO;
import com.piyush.tradex.dto.RegisterResponseDTO;
import com.piyush.tradex.enitity.User;
import com.piyush.tradex.enitity.Wallet;
import com.piyush.tradex.enums.Role;
import com.piyush.tradex.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

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

        User savedUser = userRepository.save(user);

        RegisterResponseDTO response = new RegisterResponseDTO();
        response.setUserId(savedUser.getUserId());
        response.setFirstName(savedUser.getFirstName());
        response.setLastName(savedUser.getLastName());
        response.setEmail(savedUser.getEmail());
        response.setMessage("User registered successfully");

        return response;
    }
}
