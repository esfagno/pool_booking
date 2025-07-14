package com.poolapp.pool.service;

import com.poolapp.pool.model.User;
import com.poolapp.pool.security.RegistrationRequest;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserService {
    Optional<User> findUserByEmail(String email);

    User registerUser(RegistrationRequest request);

    boolean hasActiveBooking(String email, LocalDateTime currentTime);
}

