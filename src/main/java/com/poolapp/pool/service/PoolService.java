package com.poolapp.pool.service;

import com.poolapp.pool.dto.PoolDTO;
import com.poolapp.pool.dto.PoolScheduleDTO;
import com.poolapp.pool.dto.RequestPoolDTO;
import com.poolapp.pool.dto.requestDTO.RequestPoolScheduleDTO;
import com.poolapp.pool.model.Pool;

import java.util.List;

public interface PoolService {
    PoolDTO createPool(PoolDTO pool);

    Pool getPoolByName(String name);

    List<PoolDTO> searchPools(RequestPoolDTO pool);

    PoolDTO updatePool(RequestPoolDTO updatedPool);

    void deletePool(RequestPoolDTO updatedPool);

    PoolDTO updateCapacity(RequestPoolDTO updatedPool);

    PoolScheduleDTO createOrUpdateSchedule(PoolScheduleDTO dto);

    PoolScheduleDTO updateSchedule(RequestPoolScheduleDTO dto);

    void deleteScheduleByDay(RequestPoolScheduleDTO dto);

    List<PoolScheduleDTO> getSchedulesForPool(PoolDTO dto);

    List<PoolDTO> getPoolsByDayOfWeek(Short dayOfWeek);
}