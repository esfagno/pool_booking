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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
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
    public SubscriptionTypeDTO updateSubscriptionType(SubscriptionTypeDTO newSubscriptionTypeDTO) {
        log.info("Updating SubscriptionType with name: {}", newSubscriptionTypeDTO.getName());
        SubscriptionType subscriptionType = findSubscriptionTypeByNameOrThrow(newSubscriptionTypeDTO.getName());
        subscriptionTypeMapper.updateSubscriptionTypeFromDto(subscriptionType, newSubscriptionTypeDTO);
        SubscriptionType savedSubscriptionType = subscriptionTypeRepository.save(subscriptionType);
        log.info("SubscriptionType updated with id: {}", savedSubscriptionType.getId());
        return subscriptionTypeMapper.toDto(savedSubscriptionType);
    }

    @Override
    public void deleteSubscriptionType(SubscriptionTypeDTO subscriptionTypeDTO) {
        log.info("Deleting SubscriptionType with name: {}", subscriptionTypeDTO.getName());
        SubscriptionType subscriptionType = findSubscriptionTypeByNameOrThrow(subscriptionTypeDTO.getName());
        subscriptionTypeRepository.deleteById(subscriptionType.getId());
        log.info("SubscriptionType deleted with id: {}", subscriptionType.getId());
    }

    @Override
    public List<SubscriptionTypeDTO> findSubscriptionTypesByFilter(SubscriptionTypeDTO subscriptionTypeDTO) {
        log.debug("Finding SubscriptionTypes with filter: {}", subscriptionTypeDTO);
        SubscriptionType filter = subscriptionTypeMapper.toEntity(subscriptionTypeDTO);
        Specification<SubscriptionType> spec = subscriptionTypeSpecificationBuilder.buildSpecification(filter);
        List<SubscriptionType> subscriptionTypes = subscriptionTypeRepository.findAll(spec);
        log.info("Found {} subscription types", subscriptionTypes.size());
        return subscriptionTypeMapper.toDtoList(subscriptionTypes);
    }

    private SubscriptionType findSubscriptionTypeByNameOrThrow(String name) {
        log.debug("Searching for SubscriptionType by name: {}", name);
        return subscriptionTypeRepository.findByName(name).orElseThrow(() -> {
            log.warn("SubscriptionType not found: {}", name);
            return new ModelNotFoundException(String.format(ErrorMessages.SUBSCRIPTION_NOT_FOUND, name));
        });
    }

}
