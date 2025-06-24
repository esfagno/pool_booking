package com.poolapp.pool.repository;

import com.poolapp.pool.model.Pool;
import com.poolapp.pool.repository.base.criteria.PoolCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PoolRepository extends JpaRepository<Pool, Integer>, PoolCriteria {

    Optional<Pool> findByName(String name);

    boolean existsByName(String name);

    void deleteByName(String name);

    List<Pool> findPoolByFilter(String name, String address, String description, Integer maxCapacity, Integer sessionDuration);

}
