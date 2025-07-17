package com.poolapp.pool.service;

import com.poolapp.pool.dto.UserDTO;
import com.poolapp.pool.security.JwtAuthenticationResponse;

public interface AuthService {
    JwtAuthenticationResponse register(UserDTO userDTO);

    JwtAuthenticationResponse authenticate(String email, String password);
}

