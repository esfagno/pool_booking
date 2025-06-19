package com.poolapp.pool.service.impl;

import com.poolapp.pool.model.Pool;
import com.poolapp.pool.model.PoolSchedule;
import com.poolapp.pool.repository.PoolRepository;
import com.poolapp.pool.repository.PoolScheduleRepository;
import com.poolapp.pool.service.PoolService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
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
                .orElseThrow(() -> new EntityNotFoundException("Pool not found: id=" + id));
    }

    @Override
    public List<Pool> getAllPools() {
        return poolRepository.findAll();
    }

    @Override
    public Pool updatePool(Integer id, Pool updatedPool) {
        Pool pool = getPoolById(id);
        pool.setName(updatedPool.getName());
        pool.setAddress(updatedPool.getAddress());
        pool.setDescription(updatedPool.getDescription());
        pool.setMaxCapacity(updatedPool.getMaxCapacity());
        pool.setSessionDurationMinutes(updatedPool.getSessionDurationMinutes());
        return poolRepository.save(pool);
    }

    @Override
    public void deletePool(Integer id) {
        if (!poolRepository.existsById(id)) {
            throw new EntityNotFoundException("Pool not found: id=" + id);
        }
        poolRepository.deleteById(id);
    }

    @Override
    public Pool updateCapacity(Integer poolId, Integer newCapacity) {
        Pool pool = getPoolById(poolId);
        pool.setMaxCapacity(newCapacity);
        return poolRepository.save(pool);
    }

    @Override
    public PoolSchedule createOrUpdateSchedule(Integer poolId, PoolSchedule schedule) {
        Pool pool = getPoolById(poolId);
        schedule.setPool(pool);

        return scheduleRepository.findByPoolIdAndDayOfWeek(poolId, schedule.getDayOfWeek())
                .map(existing -> {
                    existing.setOpeningTime(schedule.getOpeningTime());
                    existing.setClosingTime(schedule.getClosingTime());
                    return scheduleRepository.save(existing);
                })
                .orElseGet(() -> scheduleRepository.save(schedule));
    }

    @Override
    public PoolSchedule updateSchedule(Integer scheduleId, PoolSchedule updatedSchedule) {
        PoolSchedule existing = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found: id=" + scheduleId));

        existing.setOpeningTime(updatedSchedule.getOpeningTime());
        existing.setClosingTime(updatedSchedule.getClosingTime());
        existing.setDayOfWeek(updatedSchedule.getDayOfWeek());

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
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("DayOfWeek cannot be null");
        }
        return scheduleRepository.findPoolsByDayOfWeek(dayOfWeek);
    }

}
