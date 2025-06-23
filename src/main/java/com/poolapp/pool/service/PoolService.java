package com.poolapp.pool.service;

import com.poolapp.pool.dto.PoolDTO;
import com.poolapp.pool.dto.PoolScheduleDTO;
import com.poolapp.pool.model.Pool;

import java.util.List;

public interface PoolService {
    PoolDTO createPool(PoolDTO pool);

    Pool getPoolByName(String name);

    List<PoolDTO> searchPools(String name, String address, String description, Integer maxCapacity, Integer sessionDuration);

    PoolDTO updatePool(String poolName, PoolDTO updatedPool);

    void deletePool(PoolDTO updatedPool);

    PoolDTO updateCapacity(PoolDTO updatedPool);

    PoolScheduleDTO createOrUpdateSchedule(PoolScheduleDTO dto);

    PoolScheduleDTO updateSchedule(PoolScheduleDTO dto);

    void deleteScheduleByDay(PoolDTO poolDTO, Short dayOfWeek);

    List<PoolScheduleDTO> getSchedulesForPool(PoolDTO dto);

    List<PoolDTO> getPoolsByDayOfWeek(Short dayOfWeek);
}