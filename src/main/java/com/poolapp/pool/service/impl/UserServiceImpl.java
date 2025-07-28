package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.UpdateUserDTO;
import com.poolapp.pool.dto.UserDTO;
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
import com.poolapp.pool.util.exception.ErrorMessages;
import com.poolapp.pool.util.exception.ForbiddenOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.poolapp.pool.util.exception.ForbiddenReason.MODIFY_OTHER_USER_NOT_ALLOWED;
import static com.poolapp.pool.util.exception.ForbiddenReason.ROLE_CHANGE_NOT_ALLOWED;

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
                .orElseThrow(() -> new ModelNotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, "email: " + dto.getEmail())));

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
                .orElseThrow(() -> new ModelNotFoundException(String.format(ErrorMessages.ROLE_NOT_FOUND, roleType)));
    }

    private void validatePermissions(User requester, User target, RoleType requestedRole) {
        boolean isAdmin = securityUtil.isCurrentUserAdmin();
        boolean isOwn = requester.getEmail().equals(target.getEmail());
        boolean roleChangeAttempt = requestedRole != null;

        if (roleChangeAttempt) {
            if (!isAdmin) {
                log.warn("User {} (email: {}) attempted to change role to {} without admin privileges",
                        requester.getId(), requester.getEmail(), requestedRole);
                throw new ForbiddenOperationException(ROLE_CHANGE_NOT_ALLOWED);
            }
        }

        if (!isOwn && !isAdmin) {
            log.warn("User {} (email: {}) attempted to modify another user {} without admin privileges",
                    requester.getId(), requester.getEmail(), target.getId());
            throw new ForbiddenOperationException(MODIFY_OTHER_USER_NOT_ALLOWED);
        }
    }
}