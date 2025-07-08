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
import com.poolapp.pool.repository.specification.builder.UserSubscriptionSpecificationBuilder;
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.service.UserSubscriptionService;
import com.poolapp.pool.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionMapper userSubscriptionMapper;
    private final UserSubscriptionSpecificationBuilder userSubscriptionSpecificationBuilder;
    private final UserService userService;

    @Override
    public UserSubscriptionDTO createUserSubscription(UserSubscriptionDTO userSubscriptionDTO) {
        User user = userRepository.findByEmail(userSubscriptionDTO.getUserEmail())
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.USER_NOT_FOUND));

        Subscription subscription = subscriptionRepository.findByStatusAndSubscriptionType_Name(userSubscriptionDTO.getSubscriptionDTO().getStatus(), userSubscriptionDTO.getSubscriptionDTO().getSubscriptionTypeDTO().getName())
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.SUBSCRIPTION_NOT_FOUND));

        UserSubscription userSubscription = userSubscriptionMapper.toEntity(userSubscriptionDTO);
        userSubscription.setUser(user);
        userSubscription.setSubscription(subscription);
        userSubscription.setAssignedAt(LocalDateTime.now());

        UserSubscription saved = userSubscriptionRepository.save(userSubscription);
        return userSubscriptionMapper.toDto(saved);
    }

    @Override
    public UserSubscriptionDTO updateUserSubscription(UserSubscriptionDTO userSubscriptionDTO) {
        UserSubscription userSubscription = findUserSubscriptionByDTO(userSubscriptionDTO)
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.USER_SUBSCRIPTION_NOT_FOUND));

        userSubscriptionMapper.updateUserSubscriptionFromDto(userSubscription, userSubscriptionDTO);
        UserSubscription saved = userSubscriptionRepository.save(userSubscription);
        return userSubscriptionMapper.toDto(saved);
    }


    @Override
    public void deleteUserSubscription(UserSubscriptionDTO userSubscriptionDTO) {
        UserSubscription userSubscription = findUserSubscriptionByDTO(userSubscriptionDTO)
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.USER_SUBSCRIPTION_NOT_FOUND));
        userSubscriptionRepository.deleteById(userSubscription.getId());
    }


    @Override
    public List<UserSubscriptionDTO> findUserSubscriptionsByFilter(UserSubscriptionDTO userSubscriptionDTO) {
        UserSubscription filter = userSubscriptionMapper.toEntity(userSubscriptionDTO);
        Specification<UserSubscription> spec = userSubscriptionSpecificationBuilder.buildSpecification(filter);
        List<UserSubscription> userSubscriptions = userSubscriptionRepository.findAll(spec);
        return userSubscriptionMapper.toDtoList(userSubscriptions);
    }


    @Override
    public boolean isUserSubscriptionExpired(UserSubscriptionDTO userSubscriptionDTO, LocalDateTime now) {
        UserSubscription filter = findUserSubscriptionByDTO(userSubscriptionDTO)
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.USER_SUBSCRIPTION_NOT_FOUND));
        LocalDateTime expirationDate = filter.getAssignedAt()
                .plusDays(filter.getSubscription().getSubscriptionType().getDurationDays());
        return expirationDate.isBefore(now);
    }

    public Optional<UserSubscription> validateUserSubscription(String userEmail) {
        Specification<UserSubscription> spec = UserSubscriptionSpecification
                .isActiveAndHasRemainingBookings(0)
                .and(UserSubscriptionSpecification.hasUserEmail(userEmail));

        Optional<UserSubscription> maybeSubscription = userSubscriptionRepository.findOne(spec);

        if (maybeSubscription.isEmpty()) {
            if (userService.hasActiveBooking(userEmail, LocalDateTime.now())) {
                throw new BookingAlreadyActiveException(ErrorMessages.ALREADY_ACTIVE);
            }
        } else {
            UserSubscriptionDTO dto = userSubscriptionMapper.toDto(maybeSubscription.get());
            if (isUserSubscriptionExpired(dto, LocalDateTime.now())) {
                throw new UserSubscriptionExpiredException(ErrorMessages.USER_SUBSCRIPTION_EXPIRED);
            }
        }

        return maybeSubscription;
    }


    private Optional<UserSubscription> findUserSubscriptionByDTO(UserSubscriptionDTO userSubscriptionDTO) {
        return subscriptionRepository.findByStatusAndSubscriptionType_Name(
                        userSubscriptionDTO.getSubscriptionDTO().getStatus(),
                        userSubscriptionDTO.getSubscriptionDTO().getSubscriptionTypeDTO().getName())
                .flatMap(subscription -> userSubscriptionRepository.findByUserEmailAndSubscriptionId(
                        userSubscriptionDTO.getUserEmail(), subscription.getId()));
    }

}

