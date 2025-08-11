package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.UserSubscriptionDTO;
import com.poolapp.pool.dto.requestDTO.RequestUserSubscriptionDTO;
import com.poolapp.pool.exception.BookingAlreadyActiveException;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.exception.UserSubscriptionExpiredException;
import com.poolapp.pool.mapper.SubscriptionMapper;
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
import com.poolapp.pool.service.SubscriptionService;
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.service.UserSubscriptionService;
import com.poolapp.pool.util.exception.ApiErrorCode;
import com.poolapp.pool.util.exception.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionMapper userSubscriptionMapper;
    private final UserSubscriptionSpecificationBuilder userSubscriptionSpecificationBuilder;
    private final UserService userService;
    private final SubscriptionSpecificationBuilder subscriptionSpecificationBuilder;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionService subscriptionService;

    @Override
    public UserSubscriptionDTO createUserSubscription(UserSubscriptionDTO dto) {
        validateDto(dto);
        log.info("Creating UserSubscription for user: {}", dto.getUserEmail());

        User user = findUserByEmail(dto.getUserEmail());

        Subscription subscription = subscriptionService.findOrCreateBySubscriptionDTO(dto.getSubscriptionDTO());

        UserSubscription entity = buildUserSubscriptionEntity(dto, user, subscription);
        UserSubscription saved = userSubscriptionRepository.save(entity);

        log.info("UserSubscription created: id={}, user={}", saved.getId(), saved.getUser().getEmail());
        return userSubscriptionMapper.toDto(saved);
    }

    @Override
    public UserSubscriptionDTO updateUserSubscription(RequestUserSubscriptionDTO requestDto) {
        validateDto(requestDto);
        UserSubscription userSubscription = findUserSubscriptionByRequestDto(requestDto).orElseThrow(() -> new ModelNotFoundException(ApiErrorCode.NOT_FOUND, requestDto.getUserEmail()));

        userSubscriptionMapper.updateUserSubscriptionFromDto(userSubscription, requestDto);
        UserSubscription saved = userSubscriptionRepository.save(userSubscription);
        log.info("UserSubscription updated: id={}", saved.getId());
        return userSubscriptionMapper.toDto(saved);
    }

    @Override
    public void deleteUserSubscription(RequestUserSubscriptionDTO requestDto) {
        validateDto(requestDto);
        UserSubscription userSubscription = findUserSubscriptionByRequestDto(requestDto).orElseThrow(() -> new ModelNotFoundException(ApiErrorCode.NOT_FOUND, requestDto.getUserEmail()));

        userSubscriptionRepository.deleteById(userSubscription.getId());
        log.info("UserSubscription deleted: id={}", userSubscription.getId());
    }

    @Override
    public List<UserSubscriptionDTO> findUserSubscriptionsByFilter(RequestUserSubscriptionDTO requestDto) {
        validateDto(requestDto);
        UserSubscription filter = userSubscriptionMapper.toEntity(requestDto);
        Specification<UserSubscription> spec = userSubscriptionSpecificationBuilder.buildSpecification(filter);
        List<UserSubscription> userSubscriptions = userSubscriptionRepository.findAll(spec);

        log.debug("Found {} user subscriptions", userSubscriptions.size());
        return userSubscriptionMapper.toDtoList(userSubscriptions);
    }

    @Override
    public boolean isUserSubscriptionExpired(UserSubscriptionDTO userSubscriptionDTO, LocalDateTime now) {
        log.debug("Checking if UserSubscription is expired for user: {}", userSubscriptionDTO.getUserEmail());
        UserSubscription filter = findUserSubscriptionByDto(userSubscriptionDTO).orElseThrow(() -> {
            log.warn("UserSubscription not found during expiration check: user={}", userSubscriptionDTO.getUserEmail());
            return new ModelNotFoundException(ApiErrorCode.NOT_FOUND, userSubscriptionDTO.getUserEmail());
        });
        LocalDateTime expirationDate = filter.getAssignedAt().plusDays(filter.getSubscription().getSubscriptionType().getDurationDays());
        boolean expired = expirationDate.isBefore(now);
        log.debug("UserSubscription expired={} for user={}, expirationDate={}", expired, userSubscriptionDTO.getUserEmail(), expirationDate);
        return expired;
    }

    @Override
    public Optional<UserSubscription> validateUserSubscription(String userEmail) {
        log.debug("Validating UserSubscription for user: {}", userEmail);
        if (userEmail == null || userEmail.isBlank()) {
            throw new IllegalArgumentException();
        }

        Specification<UserSubscription> spec = UserSubscriptionSpecification.isActiveAndHasRemainingBookings(0).and(UserSubscriptionSpecification.hasUserEmail(userEmail));

        return userSubscriptionRepository.findOne(spec).map(subscription -> {
            if (isSubscriptionExpired(subscription)) {
                throw new UserSubscriptionExpiredException(ErrorMessages.USER_SUBSCRIPTION_EXPIRED);
            }
            return subscription;
        }).or(() -> {
            if (userService.hasActiveBooking(userEmail, LocalDateTime.now())) {
                throw new BookingAlreadyActiveException(ErrorMessages.ALREADY_ACTIVE);
            }
            return Optional.empty();
        });
    }

    private Optional<UserSubscription> findUserSubscriptionByRequestDto(RequestUserSubscriptionDTO dto) {
        UserSubscription filter = userSubscriptionMapper.toEntity(dto);
        return findUserSubscriptionByFilter(filter);
    }

    private Optional<UserSubscription> findUserSubscriptionByDto(UserSubscriptionDTO dto) {
        UserSubscription filter = userSubscriptionMapper.toEntity(dto);
        return findUserSubscriptionByFilter(filter);
    }

    private Optional<UserSubscription> findUserSubscriptionByFilter(UserSubscription filter) {
        Specification<UserSubscription> spec = userSubscriptionSpecificationBuilder.buildSpecification(filter);
        return userSubscriptionRepository.findOne(spec);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ModelNotFoundException(ApiErrorCode.NOT_FOUND, email));
    }

    private Subscription findActiveSubscription(UserSubscriptionDTO dto) {
        Subscription filter = subscriptionMapper.toEntity(dto.getSubscriptionDTO());
        Specification<Subscription> spec = subscriptionSpecificationBuilder.buildSpecification(filter);
        return subscriptionRepository.findOne(spec).orElseThrow(() -> new ModelNotFoundException(ApiErrorCode.NOT_FOUND, "Subscription"));
    }

    private UserSubscription buildUserSubscriptionEntity(UserSubscriptionDTO dto, User user, Subscription subscription) {
        UserSubscription entity = userSubscriptionMapper.toEntity(dto);
        entity.setUser(user);
        entity.setSubscription(subscription);
        entity.setAssignedAt(LocalDateTime.now());

        Integer maxBookings = subscription.getSubscriptionType().getMaxBookingsPerMonth();
        entity.setRemainingBookings(maxBookings);

        return entity;
    }

    private boolean isSubscriptionExpired(UserSubscription subscription) {
        UserSubscriptionDTO dto = userSubscriptionMapper.toDto(subscription);
        return isUserSubscriptionExpired(dto, LocalDateTime.now());
    }

    private void validateDto(Object dto) {
        if (dto == null) {
            throw new IllegalArgumentException();
        }
    }
}