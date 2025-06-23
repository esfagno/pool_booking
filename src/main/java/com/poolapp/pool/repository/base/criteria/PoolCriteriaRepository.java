package com.poolapp.pool.repository.base.criteria;

import com.poolapp.pool.model.Pool;

import java.util.List;

public interface PoolCriteriaRepository {
    List<Pool> findPoolByFilter(String name, String address, String description,
                                Integer maxCapacity, Integer sessionDuration);
}

