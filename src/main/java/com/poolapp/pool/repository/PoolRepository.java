package com.poolapp.pool.repository;

import com.poolapp.pool.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface PoolRepository extends JpaRepository<Pool, Long> {

    List<Pool> findByNameContainingIgnoreCase(String name);
}
