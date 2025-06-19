package com.poolapp.pool.service;

import com.poolapp.pool.model.Pool;
import com.poolapp.pool.model.PoolSchedule;

import java.time.LocalDateTime;
import java.util.List;

public interface PoolService {
    Pool createPool(Pool pool);

    Pool getPoolById(Integer id);

    List<Pool> getAllPools(String name);

    Pool updatePool(Integer id, Pool updatedPool);

    void deletePool(Integer id);

    Pool updateCapacity(Integer poolId, Integer newCapacity);

    PoolSchedule createOrUpdateSchedule(Integer poolId, PoolSchedule schedule);

    PoolSchedule updateSchedule(Integer scheduleId, PoolSchedule updatedSchedule);

    void deleteScheduleByDay(Integer poolId, Short dayOfWeek);

    List<PoolSchedule> getSchedulesForPool(Integer poolId);

    boolean isPoolOpenAt(Integer poolId, LocalDateTime dateTime);

    List<Pool> getPoolsByDayOfWeek(Short dayOfWeek);
}