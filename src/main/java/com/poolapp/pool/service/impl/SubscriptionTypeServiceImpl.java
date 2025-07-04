package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.SubscriptionTypeDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.SubscriptionTypeMapper;
import com.poolapp.pool.model.SubscriptionType;
import com.poolapp.pool.repository.SubscriptionTypeRepository;
import com.poolapp.pool.repository.specification.builder.SubscriptionTypeSpecificationBuilder;
import com.poolapp.pool.service.SubscriptionTypeService;
import com.poolapp.pool.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionTypeServiceImpl implements SubscriptionTypeService {

    private final SubscriptionTypeRepository subscriptionTypeRepository;
    private final SubscriptionTypeMapper subscriptionTypeMapper;
    private final SubscriptionTypeSpecificationBuilder subscriptionTypeSpecificationBuilder;

    @Override
    public SubscriptionTypeDTO createSubscriptionType(SubscriptionTypeDTO subscriptionTypeDTO) {
        SubscriptionType subscriptionType = subscriptionTypeMapper.toEntity(subscriptionTypeDTO);
        subscriptionTypeRepository.save(subscriptionType);
        return subscriptionTypeMapper.toDto(subscriptionType);
    }

    @Override
    public SubscriptionTypeDTO updateSubscriptionType(SubscriptionTypeDTO newSubscriptionTypeDTO) {
        SubscriptionType subscriptionType = findSubscriptionTypeByNameOrThrow(newSubscriptionTypeDTO.getName());
        subscriptionTypeMapper.updateSubscriptionTypeFromDto(subscriptionType, newSubscriptionTypeDTO);
        SubscriptionType savedSubscriptionType = subscriptionTypeRepository.save(subscriptionType);
        return subscriptionTypeMapper.toDto(savedSubscriptionType);
    }

    @Override
    public void deleteSubscriptionType(SubscriptionTypeDTO subscriptionTypeDTO) {
        SubscriptionType subscriptionType = findSubscriptionTypeByNameOrThrow(subscriptionTypeDTO.getName());
        subscriptionTypeRepository.deleteById(subscriptionType.getId());
    }

    @Override
    public List<SubscriptionTypeDTO> findSubscriptionTypesByFilter(SubscriptionTypeDTO subscriptionTypeDTO) {
        SubscriptionType filter = subscriptionTypeMapper.toEntity(subscriptionTypeDTO);
        Specification<SubscriptionType> spec = subscriptionTypeSpecificationBuilder.buildSpecification(filter);
        List<SubscriptionType> subscriptionTypes = subscriptionTypeRepository.findAll(spec);
        return subscriptionTypeMapper.toDtoList(subscriptionTypes);
    }

    private SubscriptionType findSubscriptionTypeByNameOrThrow(String name) {
        return subscriptionTypeRepository.findByName(name)
                .orElseThrow(() -> new ModelNotFoundException(
                        String.format(ErrorMessages.SUBSCRIPTION_NOT_FOUND, name)
                ));
    }

}
