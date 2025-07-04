package com.poolapp.pool.repository;

import com.poolapp.pool.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PoolRepository extends JpaRepository<Pool, Integer>, JpaSpecificationExecutor<Pool> {

    Optional<Pool> findByName(String name);

    boolean existsByName(String name);

    void deleteByName(String name);

}
