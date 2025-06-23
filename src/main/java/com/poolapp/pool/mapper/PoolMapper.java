package com.poolapp.pool.mapper;

import com.poolapp.pool.dto.PoolDTO;
import com.poolapp.pool.model.Pool;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface PoolMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updatePoolFromDto(@MappingTarget Pool pool, PoolDTO dto);

    Pool toEntity(PoolDTO dto);

    List<PoolDTO> toDtoList(List<Pool> pools);

    PoolDTO toDto(Pool pool);


}
