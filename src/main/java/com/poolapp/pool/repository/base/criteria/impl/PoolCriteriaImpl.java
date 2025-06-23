package com.poolapp.pool.repository.base.criteria.impl;

import com.poolapp.pool.model.Pool;
import com.poolapp.pool.repository.base.criteria.PoolCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

public class PoolCriteriaImpl implements PoolCriteria {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Pool> findPoolByFilter(String name, String address, String description,
                                       Integer maxCapacity, Integer sessionDuration) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Pool> query = cb.createQuery(Pool.class);
        Root<Pool> pool = query.from(Pool.class);

        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            predicates.add(cb.like(cb.lower(pool.get("name")), "%" + name.toLowerCase() + "%"));
        }

        if (address != null && !address.isBlank()) {
            predicates.add(cb.like(cb.lower(pool.get("address")), "%" + address.toLowerCase() + "%"));
        }

        if (description != null && !description.isBlank()) {
            predicates.add(cb.like(cb.lower(pool.get("description")), "%" + description.toLowerCase() + "%"));
        }

        if (maxCapacity != null) {
            predicates.add(cb.equal(pool.get("maxCapacity"), maxCapacity));
        }

        if (sessionDuration != null) {
            predicates.add(cb.equal(pool.get("sessionDurationMinutes"), sessionDuration));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        return entityManager.createQuery(query).getResultList();
    }
}

