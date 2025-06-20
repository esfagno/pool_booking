package com.poolapp.pool.repository;

import com.poolapp.pool.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PoolRepository extends JpaRepository<Pool, Integer> {
    @Query("SELECT p FROM Pool p WHERE :name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Pool> findByNameContainingNullable(@Param("name") String name);
}
