package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.PoolDTO;
import com.poolapp.pool.dto.PoolScheduleDTO;
import com.poolapp.pool.dto.RequestPoolDTO;
import com.poolapp.pool.dto.requestDTO.RequestPoolScheduleDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.PoolMapper;
import com.poolapp.pool.mapper.PoolScheduleMapper;
import com.poolapp.pool.model.Pool;
import com.poolapp.pool.model.PoolSchedule;
import com.poolapp.pool.repository.PoolRepository;
import com.poolapp.pool.repository.PoolScheduleRepository;
import com.poolapp.pool.repository.specification.builder.PoolSpecificationBuilder;
import com.poolapp.pool.service.PoolService;
import com.poolapp.pool.util.exception.ApiErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PoolServiceImpl implements PoolService {

    private final PoolRepository poolRepository;
    private final PoolScheduleRepository scheduleRepository;
    private final PoolMapper poolMapper;
    private final PoolScheduleMapper poolScheduleMapper;
    private final PoolSpecificationBuilder poolSpecificationBuilder;

    @Override
    public Pool getPoolByName(String name) {
        return poolRepository.findByName(name)
                .orElseThrow(() -> new ModelNotFoundException(
                        ApiErrorCode.NOT_FOUND,
                        String.format("Pool not found: %s", name)
                ));
    }

    @Override
    public PoolDTO createPool(PoolDTO dto) {
        return poolMapper.toDto(poolRepository.save(poolMapper.toEntity(dto)));
    }

    @Override
    public List<PoolDTO> searchPools(RequestPoolDTO filterDto) {
        Specification<Pool> spec = poolSpecificationBuilder.buildSpecification(
                poolMapper.toEntity(filterDto)
        );
        return poolMapper.toDtoList(poolRepository.findAll(spec));
    }

    @Override
    public PoolDTO updatePool(RequestPoolDTO updatePool) {
        Pool pool = getPoolByName(updatePool.getName());
        poolMapper.updatePoolFromRequestDto(pool, updatePool);
        return poolMapper.toDto(poolRepository.save(pool));
    }

    @Transactional
    @Override
    public void deletePool(RequestPoolDTO dto) {
        poolRepository.deleteByName(dto.getName());
    }

    @Override
    public PoolDTO updateCapacity(RequestPoolDTO dto) {
        Pool pool = getPoolByName(dto.getName());
        pool.setMaxCapacity(dto.getMaxCapacity());
        return poolMapper.toDto(poolRepository.save(pool));
    }

    @Override
    public PoolScheduleDTO createOrUpdateSchedule(PoolScheduleDTO dto) {
        Pool pool = getPoolByName(dto.getPoolName());
        PoolSchedule schedule = poolScheduleMapper.toEntity(dto, pool);

        return scheduleRepository.findByPoolNameAndDayOfWeek(pool.getName(), dto.getDayOfWeek())
                .map(existing -> saveUpdatedSchedule(existing, schedule))
                .orElseGet(() -> poolScheduleMapper.toDto(scheduleRepository.save(schedule)));
    }

    @Override
    public PoolScheduleDTO updateSchedule(RequestPoolScheduleDTO dto) {
        PoolSchedule existing = getScheduleByPoolAndDay(dto.getPoolName(), dto.getDayOfWeek());
        Pool pool = getPoolByName(dto.getPoolName());

        poolScheduleMapper.updateScheduleWith(existing, poolScheduleMapper.toEntity(dto, pool));
        existing.setPool(pool);

        return poolScheduleMapper.toDto(scheduleRepository.save(existing));
    }

    @Transactional
    @Override
    public void deleteScheduleByDay(RequestPoolScheduleDTO dto) {
        Pool pool = getPoolByName(dto.getPoolName());
        if (!scheduleRepository.existsByPoolIdAndDayOfWeek(pool.getId(), dto.getDayOfWeek())) {
            throw new ModelNotFoundException(
                    ApiErrorCode.NOT_FOUND,
                    String.format("Schedule for pool %s on day %d", pool.getName(), dto.getDayOfWeek())
            );
        }
        scheduleRepository.deleteByPoolIdAndDayOfWeek(pool.getId(), dto.getDayOfWeek());
    }

    @Override
    public List<PoolScheduleDTO> getSchedulesForPool(PoolDTO dto) {
        return scheduleRepository.findByPoolId(getPoolByName(dto.getName()).getId())
                .stream()
                .map(poolScheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PoolDTO> getPoolsByDayOfWeek(Short dayOfWeek) {
        return poolMapper.toDtoList(scheduleRepository.findPoolsByDayOfWeek(dayOfWeek));
    }

    private PoolSchedule getScheduleByPoolAndDay(String poolName, Short dayOfWeek) {
        return scheduleRepository.findByPoolNameAndDayOfWeek(poolName, dayOfWeek)
                .orElseThrow(() -> new ModelNotFoundException(
                        ApiErrorCode.NOT_FOUND,
                        String.format("Schedule for pool %s on day %d", poolName, dayOfWeek)
                ));
    }

    private PoolScheduleDTO saveUpdatedSchedule(PoolSchedule existing, PoolSchedule updated) {
        poolScheduleMapper.updateScheduleWith(existing, updated);
        return poolScheduleMapper.toDto(scheduleRepository.save(existing));
    }
}