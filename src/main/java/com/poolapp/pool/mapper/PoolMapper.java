package com.poolapp.pool.mapper;

import com.poolapp.pool.dto.PoolDTO;
import com.poolapp.pool.model.Pool;
import org.mapstruct.Builder;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.ERROR, collectionMappingStrategy = CollectionMappingStrategy.SETTER_PREFERRED)
public interface PoolMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePoolFromDto(@MappingTarget Pool pool, PoolDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Pool toEntity(PoolDTO dto);


    List<PoolDTO> toDtoList(List<Pool> pools);

    PoolDTO toDto(Pool pool);

    default Pool fromName(String name) {
        if (name == null) {
            return null;
        }
        Pool pool = new Pool();
        pool.setName(name);
        return pool;
    }
}
