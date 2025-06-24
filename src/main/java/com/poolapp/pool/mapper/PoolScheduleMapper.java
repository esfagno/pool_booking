package com.poolapp.pool.mapper;

import com.poolapp.pool.dto.PoolScheduleDTO;
import com.poolapp.pool.model.Pool;
import com.poolapp.pool.model.PoolSchedule;
import org.mapstruct.Builder;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.WARN, collectionMappingStrategy = CollectionMappingStrategy.SETTER_PREFERRED)
public interface PoolScheduleMapper {

    @Mapping(source = "pool.name", target = "poolName")
    PoolScheduleDTO toDto(PoolSchedule entity);

    PoolSchedule toEntity(PoolScheduleDTO dto, @Context Pool contextPool);

    List<PoolScheduleDTO> toDtoList(List<PoolSchedule> schedules);

    @Mapping(target = "pool", ignore = true)
    PoolSchedule updateScheduleWith(@MappingTarget PoolSchedule existing, PoolSchedule updated);
}