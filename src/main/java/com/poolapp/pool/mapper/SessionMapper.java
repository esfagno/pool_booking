package com.poolapp.pool.mapper;

import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.dto.requestDTO.RequestSessionDTO;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.service.PoolService;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {PoolMapper.class, PoolService.class}, builder = @Builder(disableBuilder = true),
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        collectionMappingStrategy = CollectionMappingStrategy.SETTER_PREFERRED)
public interface SessionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentCapacity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "pool", ignore = true)
    Session toEntity(SessionDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentCapacity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "pool", source = "requestPoolDTO")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Session toEntity(RequestSessionDTO dto);

    @Mapping(target = "poolName", source = "pool.name")
    SessionDTO toDto(Session session);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Session updateSessionWith(@MappingTarget Session existing, Session updated);

}
