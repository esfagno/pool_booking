package com.poolapp.pool.service.impl;

import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.model.Pool;
import com.poolapp.pool.model.PoolSchedule;
import com.poolapp.pool.repository.PoolRepository;
import com.poolapp.pool.repository.PoolScheduleRepository;
import com.poolapp.pool.service.PoolService;
import com.poolapp.pool.util.ErrorMessages;
import com.poolapp.pool.mapper.PoolMapper;
import com.poolapp.pool.mapper.PoolScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PoolServiceImpl implements PoolService {

    private final PoolRepository poolRepository;
    private final PoolScheduleRepository scheduleRepository;

    @Override
    public Pool createPool(Pool pool) {
        return poolRepository.save(pool);
    }

    @Override
    public Pool getPoolById(Integer id) {
        return poolRepository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.POOL_NOT_FOUND + id));
    }

    @Override
    public List<Pool> getAllPools(String name) {

        return poolRepository.findByNameContainingNullable(name);
    }

    @Transactional
    @Override
    public Pool updatePool(Integer id, Pool updatedPool) {
        Pool pool = getPoolById(id);
        Pool mergedPool = PoolMapper.updatePoolWith(pool, updatedPool);
        return poolRepository.save(mergedPool);
    }

    @Override
    public void deletePool(Integer id) {
        if (!poolRepository.existsById(id)) {
            throw new ModelNotFoundException(ErrorMessages.POOL_NOT_FOUND + id);
        }
        poolRepository.deleteById(id);
    }

    @Transactional
    @Override
    public Pool updateCapacity(Integer poolId, Integer newCapacity) {
        Pool pool = getPoolById(poolId);
        pool.setMaxCapacity(newCapacity);
        return poolRepository.save(pool);
    }

    @Transactional
    @Override
    public PoolSchedule createOrUpdateSchedule(Integer poolId, PoolSchedule schedule) {
        Pool pool = getPoolById(poolId);
        schedule.setPool(pool);

        return scheduleRepository.findByPoolIdAndDayOfWeek(poolId, schedule.getDayOfWeek())
                .map(existing -> {
                    PoolScheduleMapper.updateScheduleWith(existing, schedule);
                    return scheduleRepository.save(existing);
                })
                .orElseGet(() -> scheduleRepository.save(schedule));
    }

    @Transactional
    @Override
    public PoolSchedule updateSchedule(Integer scheduleId, PoolSchedule updatedSchedule) {
        PoolSchedule existing = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.SCHEDULE_NOT_FOUND + scheduleId));

        PoolScheduleMapper.updateScheduleWith(existing, updatedSchedule);

        return scheduleRepository.save(existing);
    }

    @Override
    public void deleteScheduleByDay(Integer poolId, Short dayOfWeek) {
        scheduleRepository.deleteByPoolIdAndDayOfWeek(poolId, dayOfWeek);
    }

    @Override
    public List<PoolSchedule> getSchedulesForPool(Integer poolId) {
        return scheduleRepository.findByPoolId(poolId);
    }

    @Override
    public boolean isPoolOpenAt(Integer poolId, LocalDateTime dateTime) {
        Short dayOfWeek = (short) dateTime.getDayOfWeek().getValue();
        LocalTime time = dateTime.toLocalTime();

        return scheduleRepository.findByPoolIdAndDayOfWeek(poolId, dayOfWeek)
                .map(schedule ->
                        !time.isBefore(schedule.getOpeningTime()) && !time.isAfter(schedule.getClosingTime()))
                .orElse(false);
    }

    @Override
    public List<Pool> getPoolsByDayOfWeek(Short dayOfWeek) {
        return scheduleRepository.findPoolsByDayOfWeek(dayOfWeek);
    }

}
