package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.SubscriptionDTO;
import com.poolapp.pool.dto.SubscriptionTypeDTO;
import com.poolapp.pool.dto.UserSubscriptionDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.UserSubscriptionMapper;
import com.poolapp.pool.model.Subscription;
import com.poolapp.pool.model.SubscriptionType;
import com.poolapp.pool.model.User;
import com.poolapp.pool.model.UserSubscription;
import com.poolapp.pool.model.enums.SubscriptionStatus;
import com.poolapp.pool.repository.SubscriptionRepository;
import com.poolapp.pool.repository.UserRepository;
import com.poolapp.pool.repository.UserSubscriptionRepository;
import com.poolapp.pool.repository.specification.builder.UserSubscriptionSpecificationBuilder;
import com.poolapp.pool.util.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserSubscriptionServiceImplTest {

    @InjectMocks
    private UserSubscriptionServiceImpl service;

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserSubscriptionMapper userSubscriptionMapper;

    @Mock
    private UserSubscriptionSpecificationBuilder userSubscriptionSpecificationBuilder;

    private UserSubscriptionDTO userSubscriptionDTO;
    private User user;
    private Subscription subscription;
    private UserSubscription userSubscriptionEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userSubscriptionDTO = new UserSubscriptionDTO();
        userSubscriptionDTO.setUserEmail("test@example.com");

        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setStatus(SubscriptionStatus.ACTIVE);
        SubscriptionTypeDTO subscriptionTypeDTO = new SubscriptionTypeDTO();
        subscriptionTypeDTO.setName("Basic");
        subscriptionTypeDTO.setDurationDays(30);
        subscriptionDTO.setSubscriptionTypeDTO(subscriptionTypeDTO);
        userSubscriptionDTO.setSubscriptionDTO(subscriptionDTO);

        userSubscriptionEntity = new UserSubscription();
        userSubscriptionEntity.setAssignedAt(LocalDateTime.now().minusDays(1));

        user = new User();

        subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        SubscriptionType subscriptionType = new SubscriptionType();
        subscriptionType.setName("Basic");
        subscriptionType.setDurationDays(30);
        subscription.setSubscriptionType(subscriptionType);
        subscription.setId(1);

        userSubscriptionEntity = new UserSubscription();
        userSubscriptionEntity.setUser(user);
        userSubscriptionEntity.setSubscription(subscription);
        userSubscriptionEntity.setAssignedAt(LocalDateTime.now().minusDays(2));
        userSubscriptionEntity.setId(42);
    }


    @Test
    void createUserSubscription_success() {
        UserSubscription entity = new UserSubscription();
        UserSubscription saved = new UserSubscription();
        UserSubscriptionDTO savedDto = new UserSubscriptionDTO();

        when(userRepository.findByEmail(userSubscriptionDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByStatusAndSubscriptionType_Name(
                userSubscriptionDTO.getSubscriptionDTO().getStatus(),
                userSubscriptionDTO.getSubscriptionDTO().getSubscriptionTypeDTO().getName()))
                .thenReturn(Optional.of(subscription));
        when(userSubscriptionMapper.toEntity(userSubscriptionDTO)).thenReturn(entity);
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenReturn(saved);
        when(userSubscriptionMapper.toDto(saved)).thenReturn(savedDto);

        UserSubscriptionDTO result = service.createUserSubscription(userSubscriptionDTO);

        assertNotNull(result);
        verify(userRepository).findByEmail(userSubscriptionDTO.getUserEmail());
        verify(subscriptionRepository).findByStatusAndSubscriptionType_Name(
                userSubscriptionDTO.getSubscriptionDTO().getStatus(),
                userSubscriptionDTO.getSubscriptionDTO().getSubscriptionTypeDTO().getName());
        verify(userSubscriptionMapper).toEntity(userSubscriptionDTO);
        verify(userSubscriptionRepository).save(entity);
        verify(userSubscriptionMapper).toDto(saved);

        assertEquals(savedDto, result);
    }

    @Test
    void createUserSubscription_userNotFound_throwsException() {
        when(userRepository.findByEmail(userSubscriptionDTO.getUserEmail())).thenReturn(Optional.empty());

        ModelNotFoundException ex = assertThrows(ModelNotFoundException.class,
                () -> service.createUserSubscription(userSubscriptionDTO));
        assertEquals(ErrorMessages.USER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void createUserSubscription_subscriptionNotFound_throwsException() {
        when(userRepository.findByEmail(userSubscriptionDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByStatusAndSubscriptionType_Name(
                userSubscriptionDTO.getSubscriptionDTO().getStatus(),
                userSubscriptionDTO.getSubscriptionDTO().getSubscriptionTypeDTO().getName()))
                .thenReturn(Optional.empty());

        ModelNotFoundException ex = assertThrows(ModelNotFoundException.class,
                () -> service.createUserSubscription(userSubscriptionDTO));
        assertEquals(ErrorMessages.SUBSCRIPTION_NOT_FOUND, ex.getMessage());
    }


    @Test
    void updateUserSubscription_success() {
        when(subscriptionRepository.findByStatusAndSubscriptionType_Name(
                userSubscriptionDTO.getSubscriptionDTO().getStatus(),
                userSubscriptionDTO.getSubscriptionDTO().getSubscriptionTypeDTO().getName()))
                .thenReturn(Optional.of(subscription));

        when(userSubscriptionRepository.findByUserEmailAndSubscriptionId(
                userSubscriptionDTO.getUserEmail(),
                subscription.getId()))
                .thenReturn(Optional.of(userSubscriptionEntity));

        doNothing().when(userSubscriptionMapper).updateUserSubscriptionFromDto(userSubscriptionEntity, userSubscriptionDTO);

        when(userSubscriptionRepository.save(userSubscriptionEntity)).thenReturn(userSubscriptionEntity);
        when(userSubscriptionMapper.toDto(userSubscriptionEntity)).thenReturn(userSubscriptionDTO);

        UserSubscriptionDTO result = service.updateUserSubscription(userSubscriptionDTO);

        assertEquals(userSubscriptionDTO, result);

        verify(subscriptionRepository).findByStatusAndSubscriptionType_Name(any(), any());
        verify(userSubscriptionRepository).findByUserEmailAndSubscriptionId(any(), any());
        verify(userSubscriptionMapper).updateUserSubscriptionFromDto(any(), any());
        verify(userSubscriptionRepository).save(any());
        verify(userSubscriptionMapper).toDto(any());
    }

    @Test
    void updateUserSubscription_userSubscriptionNotFound_throwsException() {
        when(subscriptionRepository.findByStatusAndSubscriptionType_Name(
                userSubscriptionDTO.getSubscriptionDTO().getStatus(),
                userSubscriptionDTO.getSubscriptionDTO().getSubscriptionTypeDTO().getName()))
                .thenReturn(Optional.of(subscription));

        when(userSubscriptionRepository.findByUserEmailAndSubscriptionId(
                userSubscriptionDTO.getUserEmail(),
                subscription.getId()))
                .thenReturn(Optional.empty());

        ModelNotFoundException ex = assertThrows(ModelNotFoundException.class,
                () -> service.updateUserSubscription(userSubscriptionDTO));

        assertEquals(ErrorMessages.USER_SUBSCRIPTION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void deleteUserSubscription_success() {
        when(subscriptionRepository.findByStatusAndSubscriptionType_Name(
                userSubscriptionDTO.getSubscriptionDTO().getStatus(),
                userSubscriptionDTO.getSubscriptionDTO().getSubscriptionTypeDTO().getName()))
                .thenReturn(Optional.of(subscription));

        when(userSubscriptionRepository.findByUserEmailAndSubscriptionId(
                userSubscriptionDTO.getUserEmail(),
                subscription.getId()))
                .thenReturn(Optional.of(userSubscriptionEntity));

        service.deleteUserSubscription(userSubscriptionDTO);

        verify(userSubscriptionRepository).deleteById(userSubscriptionEntity.getId());
    }


    @Test
    void deleteUserSubscription_userSubscriptionNotFound_throwsException() {
        UserSubscriptionServiceImpl spyService = Mockito.spy(service);
        when(subscriptionRepository.findByStatusAndSubscriptionType_Name(
                userSubscriptionDTO.getSubscriptionDTO().getStatus(),
                userSubscriptionDTO.getSubscriptionDTO().getSubscriptionTypeDTO().getName()))
                .thenReturn(Optional.of(subscription));

        when(userSubscriptionRepository.findByUserEmailAndSubscriptionId(userSubscriptionDTO.getUserEmail(), subscription.getId()))
                .thenReturn(Optional.empty());

        ModelNotFoundException ex = assertThrows(ModelNotFoundException.class,
                () -> spyService.deleteUserSubscription(userSubscriptionDTO));

        assertEquals(ErrorMessages.USER_SUBSCRIPTION_NOT_FOUND, ex.getMessage());
    }


    @Test
    void findUserSubscriptionsByFilter_success() {
        UserSubscription filterEntity = new UserSubscription();
        List<UserSubscription> foundEntities = List.of(new UserSubscription(), new UserSubscription());
        List<UserSubscriptionDTO> dtoList = List.of(new UserSubscriptionDTO(), new UserSubscriptionDTO());

        when(userSubscriptionMapper.toEntity(userSubscriptionDTO)).thenReturn(filterEntity);
        when(userSubscriptionSpecificationBuilder.buildSpecification(filterEntity)).thenReturn(mock(Specification.class));
        when(userSubscriptionRepository.findAll(any(Specification.class))).thenReturn(foundEntities);
        when(userSubscriptionMapper.toDtoList(foundEntities)).thenReturn(dtoList);

        List<UserSubscriptionDTO> result = service.findUserSubscriptionsByFilter(userSubscriptionDTO);

        assertEquals(dtoList, result);
        verify(userSubscriptionMapper).toEntity(userSubscriptionDTO);
        verify(userSubscriptionSpecificationBuilder).buildSpecification(filterEntity);
        verify(userSubscriptionRepository).findAll(any(Specification.class));
        verify(userSubscriptionMapper).toDtoList(foundEntities);
    }


    @Test
    void isUserSubscriptionExpired_activeAndExpired() {
        LocalDateTime assignedAt = LocalDateTime.now().minusDays(20);

        Subscription subscriptionForTest = new Subscription();
        subscriptionForTest.setStatus(SubscriptionStatus.ACTIVE);
        SubscriptionType subscriptionType = new SubscriptionType();
        subscriptionType.setDurationDays(10);
        subscriptionForTest.setSubscriptionType(subscriptionType);

        UserSubscription entity = new UserSubscription();
        entity.setSubscription(subscriptionForTest);
        entity.setAssignedAt(assignedAt);

        UserSubscriptionDTO dto = mock(UserSubscriptionDTO.class);
        when(userSubscriptionMapper.toEntity(dto)).thenReturn(entity);

        boolean expired = service.isUserSubscriptionExpired(dto, LocalDateTime.now());

        assertTrue(expired);
    }

    @Test
    void isUserSubscriptionExpired_notActive_returnsFalse() {
        Subscription subscriptionForTest = new Subscription();
        subscriptionForTest.setStatus(SubscriptionStatus.EXPIRED);
        UserSubscription entity = new UserSubscription();
        entity.setSubscription(subscriptionForTest);

        UserSubscriptionDTO dto = mock(UserSubscriptionDTO.class);
        when(userSubscriptionMapper.toEntity(dto)).thenReturn(entity);

        boolean expired = service.isUserSubscriptionExpired(dto, LocalDateTime.now());
        assertFalse(expired);
    }
}
