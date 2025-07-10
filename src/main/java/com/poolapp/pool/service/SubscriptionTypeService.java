package com.poolapp.pool.service;

import com.poolapp.pool.dto.SubscriptionTypeDTO;

import java.util.List;

public interface SubscriptionTypeService {

    SubscriptionTypeDTO createSubscriptionType(SubscriptionTypeDTO subscriptionTypeDTO);

    SubscriptionTypeDTO updateSubscriptionType(SubscriptionTypeDTO subscriptionTypeDTO);

    void deleteSubscriptionType(SubscriptionTypeDTO subscriptionTypeDTO);

    List<SubscriptionTypeDTO> findSubscriptionTypesByFilter(SubscriptionTypeDTO subscriptionTypeDTO);
}