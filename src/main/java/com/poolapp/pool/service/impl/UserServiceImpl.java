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
import com.poolapp.pool.repository.specification.builder.RoleSpecificationBuilder;
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.util.SecurityUtil;
import com.poolapp.pool.util.exception.ErrorMessages;
import com.poolapp.pool.util.exception.ForbiddenOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleSpecificationBuilder roleSpecificationBuilder;
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
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.USER_NOT_FOUND));

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
        return findUserByEmail(email).map(user -> bookingRepository.existsByUserIdAndSessionStartTimeAfter(user.getId(), currentTime)).orElse(false);
    }

    private Role getRoleByType(RoleType roleType) {
        return roleRepository.findByName(roleType)
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.ROLE_NOT_FOUND));
    }

    private void validatePermissions(User requester, User target, RoleType requestedRole) {
        boolean isAdmin = securityUtil.isCurrentUserAdmin();
        boolean isOwn = requester.getEmail().equals(target.getEmail());
        boolean roleChangeAttempt = requestedRole != null;

        if ((roleChangeAttempt && (!isAdmin || isOwn))
                || (!isOwn && !isAdmin)) {
            throw new ForbiddenOperationException(ErrorMessages.FORBIDDEN_OPERATION);
        }
    }

}

