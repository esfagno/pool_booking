package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.SubscriptionDTO;
import com.poolapp.pool.dto.SubscriptionTypeDTO;
import com.poolapp.pool.dto.requestDTO.RequestSubscriptionDTO;
import com.poolapp.pool.mapper.SubscriptionMapper;
import com.poolapp.pool.model.Subscription;
import com.poolapp.pool.model.SubscriptionType;
import com.poolapp.pool.repository.SubscriptionRepository;
import com.poolapp.pool.repository.specification.builder.SubscriptionSpecificationBuilder;
import com.poolapp.pool.service.SubscriptionService;
import com.poolapp.pool.service.SubscriptionTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionSpecificationBuilder subscriptionSpecificationBuilder;
    private final SubscriptionTypeService subscriptionTypeService;

    @Override
    @Transactional
    public SubscriptionDTO createSubscription(SubscriptionDTO subscriptionDTO) {
        SubscriptionTypeDTO typeDTO = subscriptionDTO.getSubscriptionTypeDTO();
        SubscriptionType subscriptionType = subscriptionTypeService.findByNameOrCreateNew(typeDTO);

        Subscription subscription = subscriptionMapper.toEntity(subscriptionDTO);
        subscription.setSubscriptionType(subscriptionType);

        Subscription saved = subscriptionRepository.save(subscription);

        return subscriptionMapper.toDto(saved);
    }


    public List<SubscriptionDTO> findAllSubscriptionsByFilter(RequestSubscriptionDTO filterDTO) {
        Subscription filter = subscriptionMapper.toEntity(filterDTO);
        Specification<Subscription> spec = subscriptionSpecificationBuilder.buildSpecification(filter);
        List<Subscription> subscriptions = subscriptionRepository.findAll(spec);
        return subscriptionMapper.toDtoList(subscriptions);
    }

    @Override
    @Transactional
    public Subscription findOrCreateBySubscriptionDTO(SubscriptionDTO subscriptionDTO) {
        SubscriptionType subscriptionType = subscriptionTypeService.findByNameOrCreateNew(subscriptionDTO.getSubscriptionTypeDTO());

        return subscriptionRepository
                .findBySubscriptionTypeAndStatus(subscriptionType, subscriptionDTO.getStatus())
                .orElseGet(() -> {
                    Subscription subscription = subscriptionMapper.toEntity(subscriptionDTO);
                    subscription.setSubscriptionType(subscriptionType);
                    return subscriptionRepository.save(subscription);
                });
    }

}

