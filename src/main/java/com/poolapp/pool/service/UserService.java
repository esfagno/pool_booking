package com.poolapp.pool.service;

import com.poolapp.pool.dto.UserDTO;
import com.poolapp.pool.model.User;
import com.poolapp.pool.model.enums.RoleType;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserService {
    Optional<User> findUserByEmail(String email);

    boolean hasActiveBooking(String email, LocalDateTime currentTime);

    UserDTO modifyUser(UserDTO dto);

    UserDTO createUser(UserDTO dto, RoleType roleType);
}

