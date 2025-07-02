package com.poolapp.pool.mapper;

import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.model.Pool;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.service.PoolService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {PoolMapper.class, PoolService.class}, builder = @Builder(disableBuilder = true),
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        collectionMappingStrategy = CollectionMappingStrategy.SETTER_PREFERRED)
public interface SessionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentCapacity", ignore = true)
    @Mapping(target = "pool", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Session toEntity(SessionDTO dto);

    @Mapping(target = "poolName", source = "pool.name")
    SessionDTO toDto(Session session);

    @AfterMapping
    default void setPoolFromName(SessionDTO dto, @MappingTarget Session entity, @Context PoolService poolService) {
        if (dto.getPoolName() != null) {
            try {
                entity.setPool(poolService.getPoolByName(dto.getPoolName()));
            } catch (ModelNotFoundException ex) {
                Pool newPool = new Pool();
                newPool.setName(dto.getPoolName());
                entity.setPool(newPool);
            }
        }
    }
}
