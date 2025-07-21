package com.poolapp.pool.service;

import com.poolapp.pool.dto.UserDTO;
import com.poolapp.pool.security.JwtAuthenticationResponse;
import com.poolapp.pool.security.UserLoginRequest;

public interface AuthService {
    JwtAuthenticationResponse register(UserDTO userDTO);

    JwtAuthenticationResponse authenticate(UserLoginRequest request);
}

