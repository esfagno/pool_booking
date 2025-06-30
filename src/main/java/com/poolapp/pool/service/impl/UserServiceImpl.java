package com.poolapp.pool.service.impl;

import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.model.User;
import com.poolapp.pool.repository.BookingRepository;
import com.poolapp.pool.repository.UserRepository;
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    //Spring Security??

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public boolean hasActiveBooking(String email, LocalDateTime currentTime) {
        return findUserByEmail(email)
                .map(user -> bookingRepository.existsByUserIdAndSessionStartTimeAfter(user.getId(), currentTime))
                .orElse(false);
    }
}

