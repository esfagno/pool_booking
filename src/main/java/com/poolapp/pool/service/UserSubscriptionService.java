package com.poolapp.pool.service;

import com.poolapp.pool.dto.UserSubscriptionDTO;
import com.poolapp.pool.model.UserSubscription;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserSubscriptionService {
    UserSubscriptionDTO createUserSubscription(UserSubscriptionDTO userSubscriptionDTO);

    UserSubscriptionDTO updateUserSubscription(UserSubscriptionDTO userSubscriptionDTO);

    void deleteUserSubscription(UserSubscriptionDTO userSubscriptionDTO);

    List<UserSubscriptionDTO> findUserSubscriptionsByFilter(UserSubscriptionDTO userSubscriptionDTO);

    boolean isUserSubscriptionExpired(UserSubscriptionDTO userSubscriptionDTO, LocalDateTime now);

    Optional<UserSubscription> validateUserSubscription(String userEmail);
}

