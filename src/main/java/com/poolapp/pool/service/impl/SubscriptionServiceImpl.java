package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.SubscriptionDTO;
import com.poolapp.pool.mapper.SubscriptionMapper;
import com.poolapp.pool.model.Subscription;
import com.poolapp.pool.repository.SubscriptionRepository;
import com.poolapp.pool.repository.specification.builder.SubscriptionSpecificationBuilder;
import com.poolapp.pool.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionSpecificationBuilder subscriptionSpecificationBuilder;

    @Override
    public SubscriptionDTO createSubscription(SubscriptionDTO subscriptionDTO) {
        Subscription subscription = subscriptionMapper.toEntity(subscriptionDTO);
        subscriptionRepository.save(subscription);
        return subscriptionMapper.toDto(subscription);
    }


    public List<SubscriptionDTO> findAllSubscriptionsByFilter(SubscriptionDTO filterDTO) {
        Specification<Subscription> spec = subscriptionSpecificationBuilder.buildSpecification(filterDTO);
        List<Subscription> subscriptions = subscriptionRepository.findAll(spec);
        return subscriptionMapper.toDtoList(subscriptions);
    }
}

