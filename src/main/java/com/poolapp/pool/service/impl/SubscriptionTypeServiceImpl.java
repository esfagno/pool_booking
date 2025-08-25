package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.SubscriptionTypeDTO;
import com.poolapp.pool.dto.requestDTO.RequestSubscriptionTypeDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.SubscriptionTypeMapper;
import com.poolapp.pool.model.SubscriptionType;
import com.poolapp.pool.repository.SubscriptionTypeRepository;
import com.poolapp.pool.repository.specification.builder.SubscriptionTypeSpecificationBuilder;
import com.poolapp.pool.service.SubscriptionTypeService;
import com.poolapp.pool.util.exception.ApiErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionTypeServiceImpl implements SubscriptionTypeService {

    private final SubscriptionTypeRepository subscriptionTypeRepository;
    private final SubscriptionTypeMapper subscriptionTypeMapper;
    private final SubscriptionTypeSpecificationBuilder specificationBuilder;

    @Override
    public SubscriptionTypeDTO createSubscriptionType(SubscriptionTypeDTO dto) {
        SubscriptionType entity = subscriptionTypeMapper.toEntity(dto);
        return subscriptionTypeMapper.toDto(subscriptionTypeRepository.save(entity));
    }

    @Override
    public SubscriptionTypeDTO updateSubscriptionType(RequestSubscriptionTypeDTO dto) {
        SubscriptionType entity = getSubscriptionTypeByName(dto.getName());
        subscriptionTypeMapper.updateSubscriptionTypeFromDto(entity, dto);
        return subscriptionTypeMapper.toDto(subscriptionTypeRepository.save(entity));
    }

    @Override
    public void deleteSubscriptionType(RequestSubscriptionTypeDTO dto) {
        subscriptionTypeRepository.delete(getSubscriptionTypeByName(dto.getName()));
    }

    @Override
    public List<SubscriptionTypeDTO> findSubscriptionTypesByFilter(RequestSubscriptionTypeDTO dto) {
        Specification<SubscriptionType> spec = specificationBuilder.buildSpecification(
                subscriptionTypeMapper.toEntity(dto)
        );
        return subscriptionTypeMapper.toDtoList(subscriptionTypeRepository.findAll(spec));
    }

    @Override
    public SubscriptionType findByNameOrCreateNew(SubscriptionTypeDTO dto) {
        return subscriptionTypeRepository.findByName(dto.getName())
                .orElseGet(() -> createNewSubscriptionType(dto));
    }

    private SubscriptionType getSubscriptionTypeByName(String name) {
        return subscriptionTypeRepository.findByName(name)
                .orElseThrow(() -> new ModelNotFoundException(
                        ApiErrorCode.NOT_FOUND,
                        String.format("SubscriptionType not found: %s", name)
                ));
    }

    private SubscriptionType createNewSubscriptionType(SubscriptionTypeDTO dto) {
        SubscriptionType entity = subscriptionTypeMapper.toEntity(dto);
        return subscriptionTypeRepository.save(entity);
    }
}