package com.poolapp.pool.service;

import com.poolapp.pool.dto.SubscriptionDTO;
import com.poolapp.pool.dto.requestDTO.RequestSubscriptionDTO;
import com.poolapp.pool.model.Subscription;

import java.util.List;

public interface SubscriptionService {
    SubscriptionDTO createSubscription(SubscriptionDTO subscriptionDTO);

    List<SubscriptionDTO> findAllSubscriptionsByFilter(RequestSubscriptionDTO filterDTO);

    Subscription findOrCreateBySubscriptionDTO(SubscriptionDTO subscriptionDTO);

}


