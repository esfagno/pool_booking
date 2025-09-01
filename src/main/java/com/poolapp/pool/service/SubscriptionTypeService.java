package com.poolapp.pool.service;

import com.poolapp.pool.dto.SubscriptionTypeDTO;
import com.poolapp.pool.dto.requestDTO.RequestSubscriptionTypeDTO;
import com.poolapp.pool.model.SubscriptionType;

import java.util.List;

public interface SubscriptionTypeService {

    SubscriptionTypeDTO createSubscriptionType(SubscriptionTypeDTO subscriptionTypeDTO);

    SubscriptionTypeDTO updateSubscriptionType(RequestSubscriptionTypeDTO subscriptionTypeDTO);

    void deleteSubscriptionType(RequestSubscriptionTypeDTO subscriptionTypeDTO);

    List<SubscriptionTypeDTO> findSubscriptionTypesByFilter(RequestSubscriptionTypeDTO subscriptionTypeDTO);

    SubscriptionType findByNameOrCreateNew(SubscriptionTypeDTO subscriptionTypeDTO);
}