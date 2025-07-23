package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.UserDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.UserMapper;
import com.poolapp.pool.model.User;
import com.poolapp.pool.repository.UserRepository;
import com.poolapp.pool.security.JwtAuthenticationResponse;
import com.poolapp.pool.security.JwtService;
import com.poolapp.pool.security.UserDetailsImpl;
import com.poolapp.pool.security.UserLoginRequest;
import com.poolapp.pool.service.AuthService;
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.util.exception.ErrorMessages;
import com.poolapp.pool.util.exception.EntityAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Override
    public JwtAuthenticationResponse register(UserDTO userDTO) {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new EntityAlreadyExistsException(ErrorMessages.EMAIL_IS_TAKEN);
        }

        User user = userMapper.toEntity(userService.createUser(userDTO));
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public JwtAuthenticationResponse authenticate(UserLoginRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        authenticationManager.authenticate(authentication);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ModelNotFoundException(ErrorMessages.USER_NOT_FOUND));

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}

