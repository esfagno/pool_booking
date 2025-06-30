package com.poolapp.pool.service;

import com.poolapp.pool.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserService {
    Optional<User> findUserByEmail(String email);

    boolean hasActiveBooking(String email, LocalDateTime currentTime);
}

