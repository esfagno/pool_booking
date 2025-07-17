package com.poolapp.pool.controller;

import com.poolapp.pool.dto.UserDTO;
import com.poolapp.pool.security.JwtAuthenticationResponse;
import com.poolapp.pool.security.LoginRequest;
import com.poolapp.pool.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtAuthenticationResponse> register(@Valid @RequestBody UserDTO userDTO) {
        JwtAuthenticationResponse response = authService.register(userDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(@RequestBody LoginRequest request) {
        JwtAuthenticationResponse response = authService.authenticate(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}
