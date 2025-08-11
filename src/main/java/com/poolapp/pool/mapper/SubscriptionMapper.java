package com.poolapp.pool.mapper;

import com.poolapp.pool.dto.SubscriptionDTO;
import com.poolapp.pool.dto.requestDTO.RequestSubscriptionDTO;
import com.poolapp.pool.model.Subscription;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SubscriptionTypeMapper.class}, builder = @Builder(disableBuilder = true),
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        collectionMappingStrategy = CollectionMappingStrategy.SETTER_PREFERRED)
public interface SubscriptionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptionType", source = "subscriptionTypeDTO")
    Subscription toEntity(SubscriptionDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptionType", source = "requestSubscriptionTypeDTO")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Subscription toEntity(RequestSubscriptionDTO dto);

    @Mapping(target = "subscriptionTypeDTO", source = "subscriptionType")
    SubscriptionDTO toDto(Subscription subscription);

    @Mapping(target = "subscriptionTypeDTO", source = "subscriptionType")
    List<SubscriptionDTO> toDtoList(List<Subscription> subscriptions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptionType", source = "subscriptionTypeDTO")
    void updateSubscriptionFromDto(@MappingTarget Subscription subscription, SubscriptionDTO subscriptionDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptionType", source = "requestSubscriptionTypeDTO")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSubscriptionFromDto(@MappingTarget Subscription subscription, RequestSubscriptionDTO subscriptionDTO);
}

