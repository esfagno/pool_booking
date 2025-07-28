package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.UpdateUserDTO;
import com.poolapp.pool.dto.UserDTO;
import com.poolapp.pool.exception.ForbiddenOperationException;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.UserMapper;
import com.poolapp.pool.model.Role;
import com.poolapp.pool.model.User;
import com.poolapp.pool.model.enums.RoleType;
import com.poolapp.pool.repository.BookingRepository;
import com.poolapp.pool.repository.RoleRepository;
import com.poolapp.pool.repository.UserRepository;
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.util.SecurityUtil;
import com.poolapp.pool.util.exception.ApiErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SecurityUtil securityUtil;

    @Override
    public UserDTO createUser(UserDTO dto) {
        Role role = getRoleByType(dto.getRoleType());
        User user = userMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(role);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserDTO modifyUser(UpdateUserDTO dto) {
        User requester = securityUtil.getCurrentUser();
        User target = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ModelNotFoundException(ApiErrorCode.NOT_FOUND, dto.getEmail()));

        validatePermissions(requester, target, dto.getRoleType());

        if (dto.getRoleType() != null) {
            Role newRole = getRoleByType(dto.getRoleType());
            target.setRole(newRole);
        }

        userMapper.updateUserFromUpdateDto(target, dto);

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            target.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        return userMapper.toDto(userRepository.save(target));
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean hasActiveBooking(String email, LocalDateTime currentTime) {
        return findUserByEmail(email).map(user ->
                        bookingRepository.existsByUserIdAndSessionStartTimeAfter(user.getId(), currentTime))
                .orElse(false);
    }

    private Role getRoleByType(RoleType roleType) {
        return roleRepository.findByName(roleType)
                .orElseThrow(() -> new ModelNotFoundException(ApiErrorCode.NOT_FOUND, roleType.name()));
    }

    private void validatePermissions(User requester, User target, RoleType requestedRole) {
        checkRoleChangePermission(requester, requestedRole);
        checkModifyOtherUserPermission(requester, target);
    }

    private void checkRoleChangePermission(User requester, RoleType requestedRole) {
        boolean isOwn = requester.getEmail().equalsIgnoreCase(securityUtil.getCurrentUser().getEmail());

        if (requestedRole != null) {
            if (!securityUtil.isCurrentUserAdmin()) {
                throw new ForbiddenOperationException(ApiErrorCode.ROLE_CHANGE_NOT_ALLOWED);
            }
            if (isOwn) {
                throw new ForbiddenOperationException(ApiErrorCode.ROLE_CHANGE_NOT_ALLOWED_OWN);
            }
        }
    }


    private void checkModifyOtherUserPermission(User requester, User target) {
        boolean isOwn = requester.getEmail().equals(target.getEmail());
        boolean isAdmin = securityUtil.isCurrentUserAdmin();
        if (!isOwn && !isAdmin) {
            log.warn("User {} (email: {}) attempted to modify another user {} without admin privileges",
                    requester.getId(), requester.getEmail(), target.getId());
            throw new ForbiddenOperationException(ApiErrorCode.MODIFY_OTHER_USER_NOT_ALLOWED);
        }
    }

}