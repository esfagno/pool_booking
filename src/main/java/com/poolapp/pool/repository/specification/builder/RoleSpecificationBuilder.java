package com.poolapp.pool.repository.specification.builder;


import com.poolapp.pool.model.Role;
import com.poolapp.pool.model.enums.RoleType;
import com.poolapp.pool.repository.specification.RoleSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSpecificationBuilder {

    public Specification<Role> buildSpecification(RoleType roleType) {
        Specification<Role> spec = Specification.where(null);

        if (roleType != null) {
            spec = spec.and(RoleSpecification.hasName(roleType));
        }

        return spec;
    }
}

