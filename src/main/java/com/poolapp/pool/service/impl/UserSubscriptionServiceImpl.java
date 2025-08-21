package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.UserSubscriptionDTO;
import com.poolapp.pool.dto.requestDTO.RequestUserSubscriptionDTO;
import com.poolapp.pool.exception.EntityAlreadyExistsException;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.exception.UserSubscriptionExpiredException;
import com.poolapp.pool.mapper.UserSubscriptionMapper;
import com.poolapp.pool.model.Subscription;
import com.poolapp.pool.model.User;
import com.poolapp.pool.model.UserSubscription;
import com.poolapp.pool.model.enums.BookingStatus;
import com.poolapp.pool.repository.BookingRepository;
import com.poolapp.pool.repository.UserRepository;
import com.poolapp.pool.repository.UserSubscriptionRepository;
import com.poolapp.pool.repository.specification.UserSubscriptionSpecification;
import com.poolapp.pool.repository.specification.builder.UserSubscriptionSpecificationBuilder;
import com.poolapp.pool.service.SubscriptionService;
import com.poolapp.pool.service.UserSubscriptionService;
import com.poolapp.pool.util.exception.ApiErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final UserSubscriptionMapper userSubscriptionMapper;
    private final UserSubscriptionSpecificationBuilder specificationBuilder;
    private final SubscriptionService subscriptionService;

    @Override
    public UserSubscriptionDTO createUserSubscription(UserSubscriptionDTO dto) {
        validateDto(dto);
        log.info("Creating UserSubscription for user: {}", dto.getUserEmail());

        User user = findUserByEmail(dto.getUserEmail());
        Subscription subscription = getOrCreateSubscription(dto);
        UserSubscription entity = buildSubscriptionEntity(dto, user, subscription);
        UserSubscription saved = userSubscriptionRepository.save(entity);

        log.info("UserSubscription created: id={}, user={}", saved.getId(), saved.getUser().getEmail());
        return userSubscriptionMapper.toDto(saved);
    }

    @Override
    public UserSubscriptionDTO updateUserSubscription(RequestUserSubscriptionDTO requestDto) {
        validateDto(requestDto);
        UserSubscription subscription = findSubscriptionByRequest(requestDto)
                .orElseThrow(() -> new ModelNotFoundException(
                        ApiErrorCode.NOT_FOUND,
                        "Subscription for user " + requestDto.getUserEmail()
                ));

        userSubscriptionMapper.updateUserSubscriptionFromDto(subscription, requestDto);
        UserSubscription saved = userSubscriptionRepository.save(subscription);
        log.info("UserSubscription updated: id={}", saved.getId());
        return userSubscriptionMapper.toDto(saved);
    }

    @Override
    public void deleteUserSubscription(RequestUserSubscriptionDTO requestDto) {
        validateDto(requestDto);
        UserSubscription subscription = findSubscriptionByRequest(requestDto)
                .orElseThrow(() -> new ModelNotFoundException(
                        ApiErrorCode.NOT_FOUND,
                        "Subscription for user " + requestDto.getUserEmail()
                ));

        userSubscriptionRepository.deleteById(subscription.getId());
        log.info("UserSubscription deleted: id={}", subscription.getId());
    }

    @Override
    public List<UserSubscriptionDTO> findUserSubscriptionsByFilter(RequestUserSubscriptionDTO requestDto) {
        validateDto(requestDto);
        Specification<UserSubscription> spec = buildSpecificationFromRequest(requestDto);
        List<UserSubscription> subscriptions = userSubscriptionRepository.findAll(spec);

        log.debug("Found {} user subscriptions matching filter", subscriptions.size());
        return userSubscriptionMapper.toDtoList(subscriptions);
    }

    @Override
    public boolean isUserSubscriptionExpired(UserSubscriptionDTO userSubscriptionDTO, LocalDateTime now) {
        log.debug("Checking if UserSubscription is expired for user: {}", userSubscriptionDTO.getUserEmail());

        UserSubscription subscription = findUserSubscriptionByDto(userSubscriptionDTO)
                .orElseThrow(() -> {
                    log.warn("UserSubscription not found during expiration check: user={}",
                            userSubscriptionDTO.getUserEmail());
                    return new ModelNotFoundException(ApiErrorCode.NOT_FOUND, userSubscriptionDTO.getUserEmail());
                });

        LocalDateTime expirationDate = subscription.getAssignedAt()
                .plusDays(subscription.getSubscription().getSubscriptionType().getDurationDays());

        boolean expired = expirationDate.isBefore(now);
        log.debug("UserSubscription expired={} for user={}, expirationDate={}",
                expired, userSubscriptionDTO.getUserEmail(), expirationDate);
        return expired;
    }

    @Override
    public Optional<UserSubscription> findActiveSubscriptionForUser(String userEmail) {
        log.debug("Finding active subscription for user: {}", userEmail);
        validateEmail(userEmail);

        Specification<UserSubscription> spec = specificationBuilder.buildActiveSubscriptionForUserSpec(userEmail);

        return userSubscriptionRepository.findAll(spec).stream()
                .filter(sub -> sub.getAssignedAt()
                        .plusDays(sub.getSubscription()
                                .getSubscriptionType()
                                .getDurationDays())
                        .isAfter(LocalDateTime.now()))
                .findFirst();
    }


    @Override
    public void validateUserSubscription(String userEmail) {
        log.debug("Validating subscription eligibility for user: {}", userEmail);
        validateEmail(userEmail);

        try {
            validateActiveSubscription(userEmail);
        } catch (UserSubscriptionExpiredException e) {
            log.warn("Expired subscription detected for user: {}", userEmail);
            throw e;
        } catch (ModelNotFoundException e) {
            validateNoActiveSubscription(userEmail);
        }
    }

    @Override
    @Transactional
    public void incrementRemainingBookings(Integer subscriptionId) {
        userSubscriptionRepository.findById(subscriptionId).ifPresent(sub -> {
            int maxBookings = sub.getSubscription().getSubscriptionType().getMaxBookingsPerMonth();
            int newCount = Math.min(sub.getRemainingBookings() + 1, maxBookings);
            sub.setRemainingBookings(newCount);
            userSubscriptionRepository.save(sub);
            log.debug("Incremented remaining bookings for subscription id={}. Remaining: {}",
                    subscriptionId, newCount);
        });
    }

    @Override
    @Transactional
    public void decrementRemainingBookings(Integer subscriptionId) {
        userSubscriptionRepository.findById(subscriptionId).ifPresent(sub -> {
            if (sub.getRemainingBookings() <= 0) {
                log.warn("Cannot decrement: no remaining bookings for subscription {}", subscriptionId);
                return;
            }

            sub.setRemainingBookings(sub.getRemainingBookings() - 1);
            userSubscriptionRepository.save(sub);
            log.debug("Decremented remaining bookings for subscription id={}. Remaining: {}",
                    subscriptionId, sub.getRemainingBookings());
        });
    }

    private void validateActiveSubscription(String userEmail) {
        Specification<UserSubscription> spec = buildActiveSubscriptionSpec(userEmail);
        UserSubscription subscription = userSubscriptionRepository.findOne(spec)
                .orElseThrow(() -> new ModelNotFoundException(
                        ApiErrorCode.NOT_FOUND,
                        "Active subscription for user " + userEmail
                ));

        if (isSubscriptionExpired(subscription)) {
            log.warn("Subscription expired for user: {}", userEmail);
            throw new UserSubscriptionExpiredException("User subscription has expired");
        }
    }

    private void validateNoActiveSubscription(String userEmail) {
        long activeBookings = countActiveBookings(userEmail);
        if (activeBookings >= 1) {
            log.warn("User {} without subscription attempted to create second booking", userEmail);
            throw new EntityAlreadyExistsException(
                    "Subscription required. You can have only 1 active booking without subscription"
            );
        }
        log.debug("User {} without subscription has no active bookings - allowed to create booking", userEmail);
    }

    private Specification<UserSubscription> buildActiveSubscriptionSpec(String userEmail) {
        return UserSubscriptionSpecification
                .isActiveAndHasRemainingBookings(0)
                .and(UserSubscriptionSpecification.hasUserEmail(userEmail));
    }

    private Specification<UserSubscription> buildSpecificationFromRequest(RequestUserSubscriptionDTO dto) {
        UserSubscription filter = userSubscriptionMapper.toEntity(dto);
        return specificationBuilder.buildSpecification(filter);
    }

    private Subscription getOrCreateSubscription(UserSubscriptionDTO dto) {
        return subscriptionService.findOrCreateBySubscriptionDTO(dto.getSubscriptionDTO());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ModelNotFoundException(
                        ApiErrorCode.NOT_FOUND,
                        "User with email " + email
                ));
    }

    private UserSubscription buildSubscriptionEntity(
            UserSubscriptionDTO dto,
            User user,
            Subscription subscription
    ) {
        UserSubscription entity = userSubscriptionMapper.toEntity(dto);
        entity.setUser(user);
        entity.setSubscription(subscription);
        entity.setAssignedAt(LocalDateTime.now());
        entity.setRemainingBookings(subscription.getSubscriptionType().getMaxBookingsPerMonth());
        return entity;
    }

    private boolean isSubscriptionExpired(UserSubscription subscription) {
        LocalDateTime expirationDate = subscription.getAssignedAt()
                .plusDays(subscription.getSubscription().getSubscriptionType().getDurationDays());

        boolean expired = expirationDate.isBefore(LocalDateTime.now());
        log.debug("Subscription expired check: user={}, expired={}, expirationDate={}",
                subscription.getUser().getEmail(), expired, expirationDate);
        return expired;
    }

    private long countActiveBookings(String userEmail) {
        return bookingRepository.countByUser_EmailAndStatus(userEmail, BookingStatus.ACTIVE);
    }

    private Optional<UserSubscription> findSubscriptionByRequest(RequestUserSubscriptionDTO dto) {
        UserSubscription filter = userSubscriptionMapper.toEntity(dto);
        Specification<UserSubscription> spec = buildSpecificationFromRequest(dto);
        return userSubscriptionRepository.findOne(spec);
    }

    private Optional<UserSubscription> findUserSubscriptionByDto(UserSubscriptionDTO dto) {
        UserSubscription filter = userSubscriptionMapper.toEntity(dto);
        User user = userRepository.findByEmail(dto.getUserEmail())
                .orElseThrow(() -> new ModelNotFoundException(
                        ApiErrorCode.NOT_FOUND,
                        "User with email " + dto.getUserEmail()
                ));
        filter.setUser(user);

        Specification<UserSubscription> spec = specificationBuilder.buildSpecification(filter);
        return userSubscriptionRepository.findOne(spec);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("User email must not be null or blank");
        }
    }

    private void validateDto(Object dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO must not be null");
        }
    }
}