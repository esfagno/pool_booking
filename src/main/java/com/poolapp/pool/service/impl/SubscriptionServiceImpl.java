package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.SubscriptionDTO;
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
    private final SubscriptionSpecificationBuilder specificationBuilder;
    private final SubscriptionTypeService subscriptionTypeService;

    @Override
    @Transactional
    public SubscriptionDTO createSubscription(SubscriptionDTO dto) {
        return subscriptionMapper.toDto(createOrUpdateSubscription(dto, false));
    }

    @Override
    public List<SubscriptionDTO> findAllSubscriptionsByFilter(RequestSubscriptionDTO filterDTO) {
        Specification<Subscription> spec = specificationBuilder.buildSpecification(subscriptionMapper.toEntity(filterDTO));
        return subscriptionMapper.toDtoList(subscriptionRepository.findAll(spec));
    }

    @Override
    @Transactional
    public Subscription findOrCreateBySubscriptionDTO(SubscriptionDTO dto) {
        return createOrUpdateSubscription(dto, true);
    }

    private Subscription createOrUpdateSubscription(SubscriptionDTO dto, boolean returnEntity) {
        SubscriptionType subscriptionType = subscriptionTypeService.findByNameOrCreateNew(dto.getSubscriptionTypeDTO());

        return subscriptionRepository.findBySubscriptionTypeAndStatus(subscriptionType, dto.getStatus()).map(existing -> {
            subscriptionMapper.updateSubscriptionFromDto(existing, dto);
            return subscriptionRepository.save(existing);
        }).orElseGet(() -> {
            Subscription subscription = subscriptionMapper.toEntity(dto);
            subscription.setSubscriptionType(subscriptionType);
            return subscriptionRepository.save(subscription);
        });
    }
}