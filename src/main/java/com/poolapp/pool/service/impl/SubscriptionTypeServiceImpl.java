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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionTypeServiceImpl implements SubscriptionTypeService {

    private final SubscriptionTypeRepository subscriptionTypeRepository;
    private final SubscriptionTypeMapper subscriptionTypeMapper;
    private final SubscriptionTypeSpecificationBuilder subscriptionTypeSpecificationBuilder;

    @Override
    public SubscriptionTypeDTO createSubscriptionType(SubscriptionTypeDTO subscriptionTypeDTO) {
        log.info("Creating SubscriptionType with name: {}", subscriptionTypeDTO.getName());
        SubscriptionType subscriptionType = subscriptionTypeMapper.toEntity(subscriptionTypeDTO);
        subscriptionTypeRepository.save(subscriptionType);
        log.info("SubscriptionType created with id: {}", subscriptionType.getId());
        return subscriptionTypeMapper.toDto(subscriptionType);
    }

    @Override
    public SubscriptionTypeDTO updateSubscriptionType(RequestSubscriptionTypeDTO newSubscriptionTypeDTO) {
        log.info("Updating SubscriptionType with name: {}", newSubscriptionTypeDTO.getName());
        SubscriptionType subscriptionType = findSubscriptionTypeByName(newSubscriptionTypeDTO.getName());
        subscriptionTypeMapper.updateSubscriptionTypeFromDto(subscriptionType, newSubscriptionTypeDTO);
        SubscriptionType savedSubscriptionType = subscriptionTypeRepository.save(subscriptionType);
        log.info("SubscriptionType updated with id: {}", savedSubscriptionType.getId());
        return subscriptionTypeMapper.toDto(savedSubscriptionType);
    }

    @Override
    public void deleteSubscriptionType(RequestSubscriptionTypeDTO subscriptionTypeDTO) {
        log.info("Deleting SubscriptionType with name: {}", subscriptionTypeDTO.getName());
        SubscriptionType subscriptionType = findSubscriptionTypeByName(subscriptionTypeDTO.getName());
        subscriptionTypeRepository.deleteById(subscriptionType.getId());
        log.info("SubscriptionType deleted with id: {}", subscriptionType.getId());
    }

    @Override
    public List<SubscriptionTypeDTO> findSubscriptionTypesByFilter(RequestSubscriptionTypeDTO subscriptionTypeDTO) {
        log.debug("Finding SubscriptionTypes with filter: {}", subscriptionTypeDTO);
        SubscriptionType filter = subscriptionTypeMapper.toEntity(subscriptionTypeDTO);
        Specification<SubscriptionType> spec = subscriptionTypeSpecificationBuilder.buildSpecification(filter);
        List<SubscriptionType> subscriptionTypes = subscriptionTypeRepository.findAll(spec);
        log.info("Found {} subscription types", subscriptionTypes.size());
        return subscriptionTypeMapper.toDtoList(subscriptionTypes);
    }

    @Override
    public SubscriptionType findByNameOrCreateNew(SubscriptionTypeDTO subscriptionTypeDTO) {
        return subscriptionTypeRepository
                .findByName(subscriptionTypeDTO.getName())
                .orElseGet(() -> {
                    SubscriptionType newType = subscriptionTypeMapper.toEntity(subscriptionTypeDTO);
                    SubscriptionType saved = subscriptionTypeRepository.save(newType);
                    log.info("Created new SubscriptionType: id={}, name={}", saved.getId(), saved.getName());
                    return saved;
                });
    }

    private SubscriptionType findSubscriptionTypeByName(String name) {
        log.debug("Searching for SubscriptionType by name: {}", name);
        return subscriptionTypeRepository.findByName(name).orElseThrow(() -> {
            log.warn("SubscriptionType not found: {}", name);
            return new ModelNotFoundException(ApiErrorCode.NOT_FOUND, name);
        });
    }

}
