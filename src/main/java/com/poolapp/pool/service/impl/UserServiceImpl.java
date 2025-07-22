package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.UserDTO;
import com.poolapp.pool.dto.UserUpdateDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.UserMapper;
import com.poolapp.pool.model.Role;
import com.poolapp.pool.model.User;
import com.poolapp.pool.model.enums.RoleType;
import com.poolapp.pool.repository.BookingRepository;
import com.poolapp.pool.repository.RoleRepository;
import com.poolapp.pool.repository.UserRepository;
import com.poolapp.pool.repository.specification.builder.RoleSpecificationBuilder;
import com.poolapp.pool.security.JwtService;
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.util.ErrorMessages;
import com.poolapp.pool.util.ForbiddenOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
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
    private final JwtService jwtService;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        Role userRole = getRoleByType(RoleType.USER);
        User user = createUserFromDTO(userDTO, userRole);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Transactional
    public void modifyUser(UserUpdateDTO dto, String requesterEmail, boolean isAdmin) {
        User requester = findUserByEmail(requesterEmail)
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.USER_NOT_FOUND));

        User target = findUserByEmail(dto.getEmail())
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.USER_NOT_FOUND));

        boolean isChangingOwnData = requester.getEmail().equals(dto.getEmail());
        boolean isChangingOwnRole = dto.getRole() != null && isChangingOwnData;

        if (!isAdmin && !isChangingOwnData) {
            throw new ForbiddenOperationException("Вы не можете изменять других пользователей");
        }

        if (dto.getRole() != null) {
            if (!isAdmin) {
                throw new ForbiddenOperationException("Вы не можете изменять роль");
            }
            if (isChangingOwnRole) {
                throw new ForbiddenOperationException("Вы не можете изменять свою роль");
            }
            Role newRole = roleRepository.findOne(roleSpecificationBuilder.buildSpecification(dto.getRole()))
                    .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.ROLE_NOT_FOUND));
            target.setRole(newRole);
        }

        userMapper.updateUserFromUpdateDto(target, dto);

        if (dto.getPassword() != null) {
            target.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(target);
    }


    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public boolean hasActiveBooking(String email, LocalDateTime currentTime) {
        return findUserByEmail(email).map(user -> bookingRepository.existsByUserIdAndSessionStartTimeAfter(user.getId(), currentTime)).orElse(false);
    }

    private User createUserFromDTO(UserDTO userDTO, Role role) {
        User user = userMapper.toEntity(userDTO);
        user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(role);
        return user;
    }

    private Role getRoleByType(RoleType roleType) {
        Specification<Role> spec = roleSpecificationBuilder.buildSpecification(roleType);
        return roleRepository.findOne(spec).orElseThrow(() -> new ModelNotFoundException(ErrorMessages.USER_NOT_FOUND));
    }

}

