package com.poolapp.pool.service.impl;

import com.poolapp.pool.model.Pool;
import com.poolapp.pool.repository.PoolRepository;
import com.poolapp.pool.service.PoolService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PoolServiceImpl implements PoolService {

    private final PoolRepository poolRepository;

    public PoolServiceImpl(PoolRepository poolRepository) {
        this.poolRepository = poolRepository;
    }

    @Override
    public Pool createPool(Pool pool) {
        return poolRepository.save(pool);
    }

    @Override
    public Pool updatePool(Long id, Pool updatedPool) {
        Pool existing = poolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pool not found"));

        existing.setName(updatedPool.getName());
        existing.setAddress(updatedPool.getAddress());
        existing.setDescription(updatedPool.getDescription());
        existing.setMaxCapacity(updatedPool.getMaxCapacity());
        existing.setSessionDurationMinutes(updatedPool.getSessionDurationMinutes());

        return poolRepository.save(existing);
    }

    @Override
    public void deletePool(Long id) {
        if (!poolRepository.existsById(id)) {
            throw new RuntimeException("Pool not found");
        }
        poolRepository.deleteById(id);
    }

    @Override
    public Pool getPoolById(Long id) {
        return poolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pool not found"));
    }

    @Override
    public List<Pool> getAllPools() {
        return poolRepository.findAll();
    }
}
