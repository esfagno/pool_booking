package com.poolapp.pool.service;

import com.poolapp.pool.dto.UserSubscriptionDTO;
import com.poolapp.pool.dto.requestDTO.RequestUserSubscriptionDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface UserSubscriptionService {
    UserSubscriptionDTO createUserSubscription(UserSubscriptionDTO userSubscriptionDTO);

    UserSubscriptionDTO updateUserSubscription(RequestUserSubscriptionDTO requestUserSubscriptionDTO);

    void deleteUserSubscription(RequestUserSubscriptionDTO requestUserSubscriptionDTO);

    List<UserSubscriptionDTO> findUserSubscriptionsByFilter(RequestUserSubscriptionDTO requestUserSubscriptionDTO);

    boolean isUserSubscriptionExpired(UserSubscriptionDTO userSubscriptionDTO, LocalDateTime now);

    void validateUserSubscription(String userEmail);
}

