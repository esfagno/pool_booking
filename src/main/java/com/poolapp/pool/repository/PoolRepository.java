package com.poolapp.pool.repository;

import com.poolapp.pool.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PoolRepository extends JpaRepository<Pool, Integer> {
}
