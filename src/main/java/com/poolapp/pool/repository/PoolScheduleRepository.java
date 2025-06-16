package com.poolapp.pool.repository;

import com.poolapp.pool.model.PoolSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface PoolScheduleRepository extends JpaRepository<PoolSchedule, Long> {
    List<PoolSchedule> findByPoolId(Long poolId);
}
