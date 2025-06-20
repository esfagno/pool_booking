package com.poolapp.pool.mapper;

import com.poolapp.pool.model.Pool;

public class PoolMapper {

    public static Pool updatePoolWith(Pool existingPool, Pool updatedPool) {
        return existingPool.toBuilder()
                .name(updatedPool.getName())
                .address(updatedPool.getAddress())
                .description(updatedPool.getDescription())
                .maxCapacity(updatedPool.getMaxCapacity())
                .sessionDurationMinutes(updatedPool.getSessionDurationMinutes())
                .build();
    }
}
