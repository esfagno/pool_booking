package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.PoolDTO;
import com.poolapp.pool.dto.PoolScheduleDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.PoolMapper;
import com.poolapp.pool.mapper.PoolScheduleMapper;
import com.poolapp.pool.model.Pool;
import com.poolapp.pool.model.PoolSchedule;
import com.poolapp.pool.repository.PoolRepository;
import com.poolapp.pool.repository.PoolScheduleRepository;
import com.poolapp.pool.service.PoolService;
import com.poolapp.pool.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoolServiceImpl implements PoolService {

    private final PoolRepository poolRepository;
    private final PoolScheduleRepository scheduleRepository;
    private final PoolMapper poolMapper;
    private final PoolScheduleMapper poolScheduleMapper;

    private Pool getPoolByDto(PoolDTO dto) {
        return poolRepository.findByName(dto.getName())
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.POOL_NOT_FOUND + dto.getName()));
    }

    @Override
    public Pool getPoolByName(String name) {
        return poolRepository.findByName(name)
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.POOL_NOT_FOUND + name));
    }

    @Transactional
    @Override
    public PoolDTO createPool(PoolDTO dto) {
        Pool pool = poolMapper.toEntity(dto);
        Pool saved = poolRepository.save(pool);
        return poolMapper.toDto(saved);

    }


    @Override
    public List<PoolDTO> searchPools(PoolDTO filterDto) {

        List<Pool> pools = poolRepository.findPoolByFilter(filterDto.getName(),
                filterDto.getAddress(),
                filterDto.getDescription(),
                filterDto.getMaxCapacity(),
                filterDto.getSessionDurationMinutes());
        return pools.stream().map(poolMapper::toDto).toList();
    }

    @Transactional
    @Override
    public PoolDTO updatePool(String oldName, PoolDTO updatedPool) {
        Pool pool = getPoolByName(oldName);
        poolMapper.updatePoolFromDto(pool, updatedPool);
        Pool saved = poolRepository.save(pool);
        return poolMapper.toDto(saved);
    }

    @Transactional
    @Override
    public void deletePool(PoolDTO dto) {
        Pool pool = getPoolByDto(dto);
        poolRepository.deleteById(pool.getId());
    }

    @Transactional
    @Override
    public PoolDTO updateCapacity(PoolDTO dto) {
        Pool pool = getPoolByDto(dto);
        pool.setMaxCapacity(dto.getMaxCapacity());
        Pool saved = poolRepository.save(pool);
        return poolMapper.toDto(saved);
    }

    @Transactional
    @Override
    public PoolScheduleDTO createOrUpdateSchedule(PoolScheduleDTO dto) {
        Pool pool = getPoolByName(dto.getPoolName());
        PoolSchedule schedule = poolScheduleMapper.toEntity(dto, pool);

        PoolSchedule saved = scheduleRepository.findByPoolNameAndDayOfWeek(pool.getName(), dto.getDayOfWeek()).map(existing -> {
            poolScheduleMapper.updateScheduleWith(existing, schedule);
            return scheduleRepository.save(existing);
        }).orElseGet(() -> scheduleRepository.save(schedule));

        return poolScheduleMapper.toDto(saved);
    }

    @Transactional
    @Override
    public PoolScheduleDTO updateSchedule(PoolScheduleDTO dto) {
        PoolSchedule existing = scheduleRepository.findByPoolNameAndDayOfWeek(dto.getPoolName(), dto.getDayOfWeek())
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.SCHEDULE_NOT_FOUND + dto.getPoolName()));

        PoolSchedule incoming = poolScheduleMapper.toEntity(dto, existing.getPool());
        poolScheduleMapper.updateScheduleWith(existing, incoming);

        PoolSchedule saved = scheduleRepository.save(existing);
        return poolScheduleMapper.toDto(saved);
    }

    @Override
    public void deleteScheduleByDay(PoolDTO poolDTO, Short dayOfWeek) {
        Pool pool = getPoolByName(poolDTO.getName());
        if (!scheduleRepository.existsByPoolIdAndDayOfWeek(pool.getId(), dayOfWeek)) {
            throw new ModelNotFoundException(ErrorMessages.SCHEDULE_NOT_FOUND + " for poolId=" + pool.getId() + " and dayOfWeek=" + dayOfWeek);
        }
        scheduleRepository.deleteByPoolIdAndDayOfWeek(pool.getId(), dayOfWeek);
    }

    @Override
    public List<PoolScheduleDTO> getSchedulesForPool(PoolDTO dto) {

        Pool pool = getPoolByName(dto.getName());
        List<PoolSchedule> schedules = scheduleRepository.findByPoolId(pool.getId());
        return schedules.stream().map(poolScheduleMapper::toDto).toList();
    }


    @Override
    public List<PoolDTO> getPoolsByDayOfWeek(Short dayOfWeek) {
        List<Pool> pools = scheduleRepository.findPoolsByDayOfWeek(dayOfWeek);
        return pools.stream().map(poolMapper::toDto).toList();
    }

}
