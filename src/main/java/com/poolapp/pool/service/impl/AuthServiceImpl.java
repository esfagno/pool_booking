package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.UserDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.UserMapper;
import com.poolapp.pool.model.Role;
import com.poolapp.pool.model.User;
import com.poolapp.pool.model.enums.RoleType;
import com.poolapp.pool.repository.RoleRepository;
import com.poolapp.pool.repository.UserRepository;
import com.poolapp.pool.repository.specification.builder.RoleSpecificationBuilder;
import com.poolapp.pool.security.JwtAuthenticationResponse;
import com.poolapp.pool.security.JwtService;
import com.poolapp.pool.security.LoginRequest;
import com.poolapp.pool.security.UserDetailsImpl;
import com.poolapp.pool.service.AuthService;
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.util.DuplicateEntityException;
import com.poolapp.pool.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleSpecificationBuilder roleSpecificationBuilder;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Override
    public JwtAuthenticationResponse register(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DuplicateEntityException(ErrorMessages.EMAIL_IS_TAKEN);
        }

        User user = userMapper.toEntity(userService.createUser(userDTO));
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String jwtToken = jwtService.generateToken(userDetails);

        return JwtAuthenticationResponse.builder().token(jwtToken).build();
    }

    @Override
    public JwtAuthenticationResponse authenticate(LoginRequest request) {
        var authentication = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        authenticationManager.authenticate(authentication);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ModelNotFoundException(ErrorMessages.USER_NOT_FOUND));

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String jwtToken = jwtService.generateToken(userDetails);

        return JwtAuthenticationResponse.builder().token(jwtToken).build();
    }

    private Role getRoleByType(RoleType roleType) {
        var spec = roleSpecificationBuilder.buildSpecification(roleType);
        return roleRepository.findOne(spec).orElseThrow(() -> new ModelNotFoundException(ErrorMessages.USER_NOT_FOUND));
    }
}

