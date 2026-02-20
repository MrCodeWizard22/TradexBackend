package com.piyush.tradex.service;

import com.piyush.tradex.dto.RegisterRequestDTO;
import com.piyush.tradex.dto.RegisterResponseDTO;

public interface UserService {
    RegisterResponseDTO registerUser(RegisterRequestDTO request);
}
