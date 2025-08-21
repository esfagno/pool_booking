package com.poolapp.pool.service;

import com.poolapp.pool.dto.UserSubscriptionDTO;
import com.poolapp.pool.dto.requestDTO.RequestUserSubscriptionDTO;
import com.poolapp.pool.model.UserSubscription;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserSubscriptionService {
    UserSubscriptionDTO createUserSubscription(UserSubscriptionDTO userSubscriptionDTO);

    UserSubscriptionDTO updateUserSubscription(RequestUserSubscriptionDTO requestUserSubscriptionDTO);

    void deleteUserSubscription(RequestUserSubscriptionDTO requestUserSubscriptionDTO);

    List<UserSubscriptionDTO> findUserSubscriptionsByFilter(RequestUserSubscriptionDTO requestUserSubscriptionDTO);

    boolean isUserSubscriptionExpired(UserSubscriptionDTO userSubscriptionDTO, LocalDateTime now);

    void validateUserSubscription(String userEmail);

    void incrementRemainingBookings(Integer subscriptionId);

    void decrementRemainingBookings(Integer subscriptionId);

    Optional<UserSubscription> findActiveSubscriptionForUser(String userEmail);
}

