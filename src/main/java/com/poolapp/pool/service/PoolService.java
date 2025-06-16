package com.poolapp.pool.service;

import com.poolapp.pool.model.Pool;

import java.util.List;

public interface PoolService {
    Pool createPool(Pool pool);

    Pool updatePool(Long id, Pool updatedPool);

    void deletePool(Long id);

    Pool getPoolById(Long id);

    List<Pool> getAllPools();
}

