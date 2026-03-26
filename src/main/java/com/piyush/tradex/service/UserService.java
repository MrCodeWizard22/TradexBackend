package com.piyush.tradex.service;

import com.piyush.tradex.dto.LoginRequestDTO;
import com.piyush.tradex.dto.LoginResponseDTO;
import com.piyush.tradex.dto.RegisterRequestDTO;
import com.piyush.tradex.dto.RegisterResponseDTO;

public interface UserService {
    RegisterResponseDTO registerUser(RegisterRequestDTO request);
    String verifyEmail(String token);
    LoginResponseDTO loginUser(LoginRequestDTO request);
}


