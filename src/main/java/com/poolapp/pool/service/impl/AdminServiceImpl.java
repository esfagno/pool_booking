package com.poolapp.pool.service.impl;

import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.model.Role;
import com.poolapp.pool.model.User;
import com.poolapp.pool.model.enums.RoleType;
import com.poolapp.pool.repository.RoleRepository;
import com.poolapp.pool.repository.UserRepository;
import com.poolapp.pool.repository.specification.builder.RoleSpecificationBuilder;
import com.poolapp.pool.service.AdminService;
import com.poolapp.pool.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleSpecificationBuilder roleSpecificationBuilder;

    @Override
    public void promoteToAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.USER_NOT_FOUND));

        Role adminRole = getRoleByType(RoleType.ADMIN);
        user.setRole(adminRole);
        userRepository.save(user);
    }

    @Override
    public void demoteToUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.USER_NOT_FOUND));

        Role userRole = getRoleByType(RoleType.USER);
        user.setRole(userRole);
        userRepository.save(user);
    }


    private Role getRoleByType(RoleType roleType) {
        Specification<Role> spec = roleSpecificationBuilder.buildSpecification(roleType);
        return roleRepository.findOne(spec)
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.ROLE_NOT_FOUND));
    }
}
