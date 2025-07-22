package com.poolapp.pool.service;

import com.poolapp.pool.dto.UserDTO;
import com.poolapp.pool.dto.UserUpdateDTO;
import com.poolapp.pool.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserService {
    Optional<User> findUserByEmail(String email);

    UserDTO createUser(UserDTO userDTO);

    boolean hasActiveBooking(String email, LocalDateTime currentTime);

    UserDTO modifyUser(UserUpdateDTO dto);
}

