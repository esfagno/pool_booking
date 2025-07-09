package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.SubscriptionDTO;
import com.poolapp.pool.dto.SubscriptionTypeDTO;
import com.poolapp.pool.dto.UserSubscriptionDTO;
import com.poolapp.pool.exception.BookingAlreadyActiveException;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.exception.UserSubscriptionExpiredException;
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
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.util.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSubscriptionServiceImplTest {

    @Spy
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

    @Mock
    private UserService userService;

    private UserSubscriptionDTO userSubscriptionDTO;
    private User user;
    private Subscription subscription;
    private UserSubscription userSubscriptionEntity;

    @BeforeEach
    void setUp() {
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
        when(subscriptionRepository.findByStatusAndSubscriptionType_Name(userSubscriptionDTO.getSubscriptionDTO().getStatus(), userSubscriptionDTO.getSubscriptionDTO().getSubscriptionTypeDTO().getName())).thenReturn(Optional.of(subscription));
        when(userSubscriptionMapper.toEntity(userSubscriptionDTO)).thenReturn(entity);
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenReturn(saved);
        when(userSubscriptionMapper.toDto(saved)).thenReturn(savedDto);

        UserSubscriptionDTO result = service.createUserSubscription(userSubscriptionDTO);

        assertNotNull(result);
        verify(userRepository).findByEmail(userSubscriptionDTO.getUserEmail());
        verify(subscriptionRepository).findByStatusAndSubscriptionType_Name(userSubscriptionDTO.getSubscriptionDTO().getStatus(), userSubscriptionDTO.getSubscriptionDTO().getSubscriptionTypeDTO().getName());
        verify(userSubscriptionMapper).toEntity(userSubscriptionDTO);
        verify(userSubscriptionRepository).save(entity);
        verify(userSubscriptionMapper).toDto(saved);

        assertEquals(savedDto, result);
    }

    @Test
    void createUserSubscription_userNotFound_throwsException() {
        when(userRepository.findByEmail(userSubscriptionDTO.getUserEmail())).thenReturn(Optional.empty());

        ModelNotFoundException ex = assertThrows(ModelNotFoundException.class, () -> service.createUserSubscription(userSubscriptionDTO));
        assertEquals(ErrorMessages.USER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void createUserSubscription_subscriptionNotFound_throwsException() {
        when(userRepository.findByEmail(userSubscriptionDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByStatusAndSubscriptionType_Name(userSubscriptionDTO.getSubscriptionDTO().getStatus(), userSubscriptionDTO.getSubscriptionDTO().getSubscriptionTypeDTO().getName())).thenReturn(Optional.empty());

        ModelNotFoundException ex = assertThrows(ModelNotFoundException.class, () -> service.createUserSubscription(userSubscriptionDTO));
        assertEquals(ErrorMessages.SUBSCRIPTION_NOT_FOUND, ex.getMessage());
    }


    @Test
    void updateUserSubscription_success() {
        Specification<UserSubscription> spec = Specification.where(null);

        when(userSubscriptionMapper.toEntity(userSubscriptionDTO)).thenReturn(userSubscriptionEntity);
        when(userSubscriptionSpecificationBuilder.buildSpecification(userSubscriptionEntity)).thenReturn(spec);
        when(userSubscriptionRepository.findOne(spec)).thenReturn(Optional.of(userSubscriptionEntity));

        doNothing().when(userSubscriptionMapper).updateUserSubscriptionFromDto(userSubscriptionEntity, userSubscriptionDTO);
        when(userSubscriptionRepository.save(userSubscriptionEntity)).thenReturn(userSubscriptionEntity);
        when(userSubscriptionMapper.toDto(userSubscriptionEntity)).thenReturn(userSubscriptionDTO);

        UserSubscriptionDTO result = service.updateUserSubscription(userSubscriptionDTO);

        assertEquals(userSubscriptionDTO, result);

        verify(userSubscriptionMapper).toEntity(userSubscriptionDTO);
        verify(userSubscriptionSpecificationBuilder).buildSpecification(userSubscriptionEntity);
        verify(userSubscriptionRepository).findOne(spec);
        verify(userSubscriptionMapper).updateUserSubscriptionFromDto(userSubscriptionEntity, userSubscriptionDTO);
        verify(userSubscriptionRepository).save(userSubscriptionEntity);
        verify(userSubscriptionMapper).toDto(userSubscriptionEntity);
    }


    @Test
    void updateUserSubscription_userSubscriptionNotFound_throwsException() {
        Specification<UserSubscription> spec = Specification.where(null);

        when(userSubscriptionMapper.toEntity(userSubscriptionDTO)).thenReturn(userSubscriptionEntity);
        when(userSubscriptionSpecificationBuilder.buildSpecification(userSubscriptionEntity)).thenReturn(spec);
        when(userSubscriptionRepository.findOne(spec)).thenReturn(Optional.empty());

        ModelNotFoundException ex = assertThrows(ModelNotFoundException.class, () -> service.updateUserSubscription(userSubscriptionDTO));

        assertEquals(ErrorMessages.USER_SUBSCRIPTION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void deleteUserSubscription_success() {
        Specification<UserSubscription> spec = Specification.where(null);
        when(userSubscriptionMapper.toEntity(userSubscriptionDTO)).thenReturn(userSubscriptionEntity);
        when(userSubscriptionSpecificationBuilder.buildSpecification(userSubscriptionEntity)).thenReturn(spec);
        when(userSubscriptionRepository.findOne(spec)).thenReturn(Optional.of(userSubscriptionEntity));

        service.deleteUserSubscription(userSubscriptionDTO);

        verify(userSubscriptionRepository).deleteById(userSubscriptionEntity.getId());
    }


    @Test
    void deleteUserSubscription_userSubscriptionNotFound_throwsException() {
        UserSubscriptionServiceImpl spyService = Mockito.spy(service);
        Specification<UserSubscription> spec = Specification.where(null);

        when(userSubscriptionMapper.toEntity(userSubscriptionDTO)).thenReturn(userSubscriptionEntity);
        when(userSubscriptionSpecificationBuilder.buildSpecification(userSubscriptionEntity)).thenReturn(spec);
        when(userSubscriptionRepository.findOne(spec)).thenReturn(Optional.empty());

        ModelNotFoundException ex = assertThrows(ModelNotFoundException.class, () -> service.deleteUserSubscription(userSubscriptionDTO));

        assertEquals(ErrorMessages.USER_SUBSCRIPTION_NOT_FOUND, ex.getMessage());

        verify(userSubscriptionMapper).toEntity(userSubscriptionDTO);
        verify(userSubscriptionSpecificationBuilder).buildSpecification(userSubscriptionEntity);
        verify(userSubscriptionRepository).findOne(spec);
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
        subscriptionType.setName("name");
        subscriptionType.setDurationDays(10);
        subscriptionForTest.setSubscriptionType(subscriptionType);

        SubscriptionTypeDTO subscriptionTypeDTO = new SubscriptionTypeDTO();
        subscriptionTypeDTO.setDurationDays(10);
        subscriptionTypeDTO.setName("name");

        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionDTO.setSubscriptionTypeDTO(subscriptionTypeDTO);

        UserSubscriptionDTO userSubscriptionDTO = new UserSubscriptionDTO();
        userSubscriptionDTO.setSubscriptionDTO(subscriptionDTO);
        userSubscriptionDTO.setUserEmail("test@example.com");

        UserSubscription entity = new UserSubscription();
        entity.setSubscription(subscriptionForTest);
        entity.setAssignedAt(assignedAt);


        Specification<UserSubscription> spec = Specification.where(null);

        when(userSubscriptionMapper.toEntity(userSubscriptionDTO)).thenReturn(entity);
        when(userSubscriptionSpecificationBuilder.buildSpecification(entity)).thenReturn(spec);
        when(userSubscriptionRepository.findOne(spec)).thenReturn(Optional.of(entity));

        boolean expired = service.isUserSubscriptionExpired(userSubscriptionDTO, LocalDateTime.now());

        assertTrue(expired);
    }


    @Test
    void isUserSubscriptionExpired_beforeExpiration_returnsFalse() {
        LocalDateTime assignedAt = LocalDateTime.now().minusDays(1);

        Subscription subscriptionForTest = new Subscription();
        subscriptionForTest.setStatus(SubscriptionStatus.ACTIVE);
        SubscriptionType subscriptionType = new SubscriptionType();
        subscriptionType.setName("name");
        subscriptionType.setDurationDays(10);
        subscriptionForTest.setSubscriptionType(subscriptionType);

        SubscriptionTypeDTO subscriptionTypeDTO = new SubscriptionTypeDTO();
        subscriptionTypeDTO.setDurationDays(10);
        subscriptionTypeDTO.setName("name");

        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionDTO.setSubscriptionTypeDTO(subscriptionTypeDTO);

        UserSubscriptionDTO userSubscriptionDTO = new UserSubscriptionDTO();
        userSubscriptionDTO.setSubscriptionDTO(subscriptionDTO);
        userSubscriptionDTO.setUserEmail("test@example.com");

        UserSubscription entity = new UserSubscription();
        entity.setSubscription(subscriptionForTest);
        entity.setAssignedAt(assignedAt);

        Specification<UserSubscription> spec = Specification.where(null);

        when(userSubscriptionMapper.toEntity(userSubscriptionDTO)).thenReturn(entity);
        when(userSubscriptionSpecificationBuilder.buildSpecification(entity)).thenReturn(spec);
        when(userSubscriptionRepository.findOne(spec)).thenReturn(Optional.of(entity));

        boolean expired = service.isUserSubscriptionExpired(userSubscriptionDTO, LocalDateTime.now());

        assertFalse(expired);
    }


    @Test
    void isUserSubscriptionExpired_afterExpiration_returnsTrue() {
        LocalDateTime assignedAt = LocalDateTime.now().minusDays(15);

        Subscription subscriptionForTest = new Subscription();
        subscriptionForTest.setStatus(SubscriptionStatus.ACTIVE);
        SubscriptionType subscriptionType = new SubscriptionType();
        subscriptionType.setName("name");
        subscriptionType.setDurationDays(10);
        subscriptionForTest.setSubscriptionType(subscriptionType);

        SubscriptionTypeDTO subscriptionTypeDTO = new SubscriptionTypeDTO();
        subscriptionTypeDTO.setDurationDays(10);
        subscriptionTypeDTO.setName("name");

        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionDTO.setSubscriptionTypeDTO(subscriptionTypeDTO);

        UserSubscriptionDTO userSubscriptionDTO = new UserSubscriptionDTO();
        userSubscriptionDTO.setSubscriptionDTO(subscriptionDTO);
        userSubscriptionDTO.setUserEmail("test@example.com");

        UserSubscription entity = new UserSubscription();
        entity.setSubscription(subscriptionForTest);
        entity.setAssignedAt(assignedAt);

        Specification<UserSubscription> spec = Specification.where(null);

        when(userSubscriptionMapper.toEntity(userSubscriptionDTO)).thenReturn(entity);
        when(userSubscriptionSpecificationBuilder.buildSpecification(entity)).thenReturn(spec);
        when(userSubscriptionRepository.findOne(spec)).thenReturn(Optional.of(entity));

        boolean expired = service.isUserSubscriptionExpired(userSubscriptionDTO, LocalDateTime.now());

        assertTrue(expired);

        verify(userSubscriptionMapper).toEntity(userSubscriptionDTO);
        verify(userSubscriptionSpecificationBuilder).buildSpecification(entity);
        verify(userSubscriptionRepository).findOne(spec);
    }

    @Test
    void validateUserSubscription_noSubscriptionAndHasActiveBooking_throwsBookingAlreadyActive() {
        String email = "user@example.com";
        when(userSubscriptionRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(userService.hasActiveBooking(eq(email), any(LocalDateTime.class))).thenReturn(true);

        BookingAlreadyActiveException ex = assertThrows(BookingAlreadyActiveException.class, () -> service.validateUserSubscription(email));
        assertEquals(ErrorMessages.ALREADY_ACTIVE, ex.getMessage());
    }

    @Test
    void validateUserSubscription_noSubscriptionAndNoActiveBooking_returnsEmpty() {
        String email = "user@example.com";
        when(userSubscriptionRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(userService.hasActiveBooking(eq(email), any(LocalDateTime.class))).thenReturn(false);

        Optional<UserSubscription> result = service.validateUserSubscription(email);
        assertFalse(result.isPresent());
    }

    @Test
    void validateUserSubscription_subscriptionExpired_throwsUserSubscriptionExpired() {
        String email = "user@example.com";
        UserSubscription expired = new UserSubscription();
        expired.setAssignedAt(LocalDateTime.now().minusDays(10));
        Subscription sub = new Subscription();
        SubscriptionType st = new SubscriptionType();
        st.setDurationDays(5);
        sub.setSubscriptionType(st);
        expired.setSubscription(sub);

        when(userSubscriptionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(expired));
        UserSubscriptionDTO dto = mock(UserSubscriptionDTO.class);
        when(userSubscriptionMapper.toDto(expired)).thenReturn(dto);
        doReturn(true).when(service).isUserSubscriptionExpired(any(UserSubscriptionDTO.class), any(LocalDateTime.class));


        UserSubscriptionExpiredException ex = assertThrows(UserSubscriptionExpiredException.class, () -> service.validateUserSubscription(email));
        assertEquals(ErrorMessages.USER_SUBSCRIPTION_EXPIRED, ex.getMessage());
    }

    @Test
    void validateUserSubscription_subscriptionActiveAndNotExpired_returnsOptional() {
        String email = "user@example.com";
        UserSubscription valid = new UserSubscription();
        valid.setAssignedAt(LocalDateTime.now().minusDays(1));
        Subscription sub = new Subscription();
        SubscriptionType st = new SubscriptionType();
        st.setDurationDays(5);
        sub.setSubscriptionType(st);
        valid.setSubscription(sub);

        when(userSubscriptionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(valid));
        UserSubscriptionDTO dto = mock(UserSubscriptionDTO.class);
        when(userSubscriptionMapper.toDto(valid)).thenReturn(dto);
        doReturn(false).when(service).isUserSubscriptionExpired(any(UserSubscriptionDTO.class), any(LocalDateTime.class));


        Optional<UserSubscription> result = service.validateUserSubscription(email);
        assertTrue(result.isPresent());
        assertSame(valid, result.get());
    }

}
