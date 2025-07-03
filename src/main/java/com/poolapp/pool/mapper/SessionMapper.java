package com.poolapp.pool.mapper;

import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.service.PoolService;
import org.mapstruct.Builder;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {PoolMapper.class, PoolService.class}, builder = @Builder(disableBuilder = true),
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        collectionMappingStrategy = CollectionMappingStrategy.SETTER_PREFERRED)
public interface SessionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentCapacity", ignore = true)
    @Mapping(target = "pool", source = "poolDTO")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Session toEntity(SessionDTO dto);

    @Mapping(target = "poolDTO", source = "pool")
    SessionDTO toDto(Session session);

}
