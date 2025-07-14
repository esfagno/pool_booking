package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.UserSubscriptionDTO;
import com.poolapp.pool.exception.BookingAlreadyActiveException;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.exception.UserSubscriptionExpiredException;
import com.poolapp.pool.mapper.UserSubscriptionMapper;
import com.poolapp.pool.model.Subscription;
import com.poolapp.pool.model.User;
import com.poolapp.pool.model.UserSubscription;
import com.poolapp.pool.repository.SubscriptionRepository;
import com.poolapp.pool.repository.UserRepository;
import com.poolapp.pool.repository.UserSubscriptionRepository;
import com.poolapp.pool.repository.specification.UserSubscriptionSpecification;
import com.poolapp.pool.repository.specification.builder.SubscriptionSpecificationBuilder;
import com.poolapp.pool.repository.specification.builder.UserSubscriptionSpecificationBuilder;
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.service.UserSubscriptionService;
import com.poolapp.pool.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionMapper userSubscriptionMapper;
    private final UserSubscriptionSpecificationBuilder userSubscriptionSpecificationBuilder;
    private final UserService userService;
    private final SubscriptionSpecificationBuilder subscriptionSpecificationBuilder;

    @Override
    public UserSubscriptionDTO createUserSubscription(UserSubscriptionDTO userSubscriptionDTO) {
        log.info("Creating UserSubscription for user: {}", userSubscriptionDTO.getUserEmail());
        User user = userRepository.findByEmail(userSubscriptionDTO.getUserEmail()).orElseThrow(() -> {
            log.warn("User not found: {}", userSubscriptionDTO.getUserEmail());
            return new ModelNotFoundException(ErrorMessages.USER_NOT_FOUND);
        });

        Specification<Subscription> spec = subscriptionSpecificationBuilder.buildSpecification(userSubscriptionDTO.getSubscriptionDTO());
        Subscription subscription = subscriptionRepository.findOne(spec).orElseThrow(() -> {
            log.warn("Subscription not found: status={}, type={}", userSubscriptionDTO.getSubscriptionDTO().getStatus(), userSubscriptionDTO.getSubscriptionDTO().getSubscriptionTypeDTO().getName());
            return new ModelNotFoundException(ErrorMessages.SUBSCRIPTION_NOT_FOUND);
        });


        UserSubscription userSubscription = userSubscriptionMapper.toEntity(userSubscriptionDTO);
        userSubscription.setUser(user);
        userSubscription.setSubscription(subscription);
        userSubscription.setAssignedAt(LocalDateTime.now());

        UserSubscription saved = userSubscriptionRepository.save(userSubscription);
        log.info("UserSubscription created: id={}, user={}", saved.getId(), user.getEmail());
        return userSubscriptionMapper.toDto(saved);
    }

    @Override
    public UserSubscriptionDTO updateUserSubscription(UserSubscriptionDTO userSubscriptionDTO) {
        UserSubscription userSubscription = findUserSubscriptionByDTO(userSubscriptionDTO).orElseThrow(() -> {
            log.warn("UserSubscription not found for update: user={}", userSubscriptionDTO.getUserEmail());
            return new ModelNotFoundException(ErrorMessages.USER_SUBSCRIPTION_NOT_FOUND);
        });

        userSubscriptionMapper.updateUserSubscriptionFromDto(userSubscription, userSubscriptionDTO);
        UserSubscription saved = userSubscriptionRepository.save(userSubscription);
        log.info("UserSubscription updated: id={}", saved.getId());
        return userSubscriptionMapper.toDto(saved);
    }


    @Override
    public void deleteUserSubscription(UserSubscriptionDTO userSubscriptionDTO) {
        log.info("Deleting UserSubscription for user: {}", userSubscriptionDTO.getUserEmail());
        UserSubscription userSubscription = findUserSubscriptionByDTO(userSubscriptionDTO).orElseThrow(() -> {
            log.warn("UserSubscription not found for deletion: user={}", userSubscriptionDTO.getUserEmail());
            return new ModelNotFoundException(ErrorMessages.USER_SUBSCRIPTION_NOT_FOUND);
        });
        userSubscriptionRepository.deleteById(userSubscription.getId());
        log.info("UserSubscription deleted: id={}", userSubscription.getId());

    }


    @Override
    public List<UserSubscriptionDTO> findUserSubscriptionsByFilter(UserSubscriptionDTO userSubscriptionDTO) {
        log.debug("Finding UserSubscriptions with filter: {}", userSubscriptionDTO);
        UserSubscription filter = userSubscriptionMapper.toEntity(userSubscriptionDTO);
        Specification<UserSubscription> spec = userSubscriptionSpecificationBuilder.buildSpecification(filter);
        List<UserSubscription> userSubscriptions = userSubscriptionRepository.findAll(spec);
        log.debug("Found {} user subscriptions", userSubscriptions.size());
        return userSubscriptionMapper.toDtoList(userSubscriptions);
    }


    @Override
    public boolean isUserSubscriptionExpired(UserSubscriptionDTO userSubscriptionDTO, LocalDateTime now) {
        log.debug("Checking if UserSubscription is expired for user: {}", userSubscriptionDTO.getUserEmail());
        UserSubscription filter = findUserSubscriptionByDTO(userSubscriptionDTO).orElseThrow(() -> {
            log.warn("UserSubscription not found during expiration check: user={}", userSubscriptionDTO.getUserEmail());
            return new ModelNotFoundException(ErrorMessages.USER_SUBSCRIPTION_NOT_FOUND);
        });
        LocalDateTime expirationDate = filter.getAssignedAt().plusDays(filter.getSubscription().getSubscriptionType().getDurationDays());
        boolean expired = expirationDate.isBefore(now);
        log.debug("UserSubscription expired={} for user={}, expirationDate={}", expired, userSubscriptionDTO.getUserEmail(), expirationDate);
        return expired;
    }

    public Optional<UserSubscription> validateUserSubscription(String userEmail) {
        log.debug("Validating UserSubscription for user: {}", userEmail);
        Specification<UserSubscription> spec = UserSubscriptionSpecification.isActiveAndHasRemainingBookings(0).and(UserSubscriptionSpecification.hasUserEmail(userEmail));

        Optional<UserSubscription> maybeSubscription = userSubscriptionRepository.findOne(spec);

        if (maybeSubscription.isEmpty()) {
            log.warn("No valid UserSubscription found for user: {}", userEmail);
            if (userService.hasActiveBooking(userEmail, LocalDateTime.now())) {
                log.warn("User has active booking without valid subscription: {}", userEmail);
                throw new BookingAlreadyActiveException(ErrorMessages.ALREADY_ACTIVE);
            }
        } else {
            UserSubscriptionDTO dto = userSubscriptionMapper.toDto(maybeSubscription.get());
            if (isUserSubscriptionExpired(dto, LocalDateTime.now())) {
                log.warn("UserSubscription is expired for user: {}", userEmail);
                throw new UserSubscriptionExpiredException(ErrorMessages.USER_SUBSCRIPTION_EXPIRED);
            }
        }

        return maybeSubscription;
    }


    private Optional<UserSubscription> findUserSubscriptionByDTO(UserSubscriptionDTO dto) {
        log.debug("Finding UserSubscription by DTO: user={}, subscriptionType={}", dto.getUserEmail(), dto.getSubscriptionDTO().getSubscriptionTypeDTO().getName());

        UserSubscription filter = userSubscriptionMapper.toEntity(dto);
        Specification<UserSubscription> spec = userSubscriptionSpecificationBuilder.buildSpecification(filter);

        return userSubscriptionRepository.findOne(spec);
    }


}

