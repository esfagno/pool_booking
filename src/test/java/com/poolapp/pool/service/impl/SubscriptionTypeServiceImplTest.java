package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.SubscriptionTypeDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.SubscriptionTypeMapper;
import com.poolapp.pool.model.SubscriptionType;
import com.poolapp.pool.repository.SubscriptionTypeRepository;
import com.poolapp.pool.repository.specification.builder.SubscriptionTypeSpecificationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubscriptionTypeServiceImplTest {

    @Mock
    private SubscriptionTypeRepository subscriptionTypeRepository;

    @Spy
    private SubscriptionTypeMapper subscriptionTypeMapper = Mappers.getMapper(SubscriptionTypeMapper.class);

    @Spy
    private SubscriptionTypeSpecificationBuilder specificationBuilder;

    @InjectMocks
    private SubscriptionTypeServiceImpl subscriptionTypeService;

    private SubscriptionType subscriptionType;
    private SubscriptionTypeDTO subscriptionTypeDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        subscriptionType = new SubscriptionType();

        subscriptionType.setName("Basic");
        subscriptionType.setId(1);
        subscriptionType.setPrice(BigDecimal.valueOf(50));
        subscriptionType.setDurationDays(30);
        subscriptionType.setMaxBookingsPerMonth(10);
        subscriptionType.setDescription("Basic subscription");

        subscriptionTypeDTO = SubscriptionTypeDTO.builder()
                .name("Basic")
                .maxBookingsPerMonth(10)
                .price(BigDecimal.valueOf(50))
                .durationDays(30)
                .description("Basic subscription")
                .build();
    }

    @Test
    void test_createSubscriptionType_shouldReturnSavedDTO() {
        when(subscriptionTypeRepository.save(any())).thenReturn(subscriptionType);

        SubscriptionTypeDTO result = subscriptionTypeService.createSubscriptionType(subscriptionTypeDTO);

        assertNotNull(result);
        assertEquals(subscriptionType.getName(), result.getName());
        verify(subscriptionTypeRepository, times(1)).save(any());
    }

    @Test
    void test_updateSubscriptionType_shouldUpdateAndReturnDTO() {
        when(subscriptionTypeRepository.findByName("Basic")).thenReturn(Optional.of(subscriptionType));
        when(subscriptionTypeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionTypeDTO updatedDTO = SubscriptionTypeDTO.builder()
                .name("Basic")
                .maxBookingsPerMonth(15)
                .price(BigDecimal.valueOf(70))
                .durationDays(60)
                .description("Updated subscription")
                .build();

        SubscriptionTypeDTO result = subscriptionTypeService.updateSubscriptionType(updatedDTO);

        assertNotNull(result);
        assertEquals(updatedDTO.getMaxBookingsPerMonth(), result.getMaxBookingsPerMonth());
        assertEquals(updatedDTO.getPrice(), result.getPrice());
        verify(subscriptionTypeRepository, times(1)).save(any());
    }

    @Test
    void test_updateSubscriptionType_shouldThrowIfNotFound() {
        when(subscriptionTypeRepository.findByName("Basic")).thenReturn(Optional.empty());

        assertThrows(ModelNotFoundException.class, () -> subscriptionTypeService.updateSubscriptionType(subscriptionTypeDTO));
        verify(subscriptionTypeRepository, times(0)).save(any());
    }

    @Test
    void test_deleteSubscriptionType_shouldDeleteIfExists() {
        when(subscriptionTypeRepository.findByName("Basic")).thenReturn(Optional.of(subscriptionType));

        subscriptionTypeService.deleteSubscriptionType(subscriptionTypeDTO);

        verify(subscriptionTypeRepository, times(1)).deleteById(subscriptionType.getId());
    }

    @Test
    void test_deleteSubscriptionType_shouldThrowIfNotFound() {
        when(subscriptionTypeRepository.findByName("Basic")).thenReturn(Optional.empty());

        assertThrows(ModelNotFoundException.class, () -> subscriptionTypeService.deleteSubscriptionType(subscriptionTypeDTO));
        verify(subscriptionTypeRepository, times(0)).deleteById(any());
    }

    @Test
    void test_findSubscriptionTypesByFilter_shouldReturnFilteredList() {
        SubscriptionTypeDTO filterDto = SubscriptionTypeDTO.builder()
                .name("Basic")
                .build();

        when(subscriptionTypeRepository.findAll(any(Specification.class))).thenReturn(List.of(subscriptionType));

        List<SubscriptionTypeDTO> result = subscriptionTypeService.findSubscriptionTypesByFilter(filterDto);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Basic", result.get(0).getName());
    }

    @Test
    void test_findSubscriptionTypesByFilter_shouldReturnAllIfNoFilter() {
        SubscriptionTypeDTO filterDto = SubscriptionTypeDTO.builder().build();

        when(subscriptionTypeRepository.findAll(any(Specification.class))).thenReturn(List.of(subscriptionType));

        List<SubscriptionTypeDTO> result = subscriptionTypeService.findSubscriptionTypesByFilter(filterDto);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Basic", result.get(0).getName());
    }
}
