package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.PoolDTO;
import com.poolapp.pool.dto.PoolScheduleDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.PoolMapper;
import com.poolapp.pool.mapper.PoolScheduleMapper;
import com.poolapp.pool.model.Pool;
import com.poolapp.pool.model.PoolSchedule;
import com.poolapp.pool.repository.PoolRepository;
import com.poolapp.pool.repository.PoolScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PoolServiceImplTest {

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private PoolScheduleRepository scheduleRepository;

    @Spy
    private PoolMapper poolMapper = Mappers.getMapper(PoolMapper.class);

    @Spy
    private PoolScheduleMapper poolScheduleMapper = Mappers.getMapper(PoolScheduleMapper.class);

    @InjectMocks
    private PoolServiceImpl poolService;

    private Pool pool;
    private PoolDTO poolDTO;
    private PoolSchedule schedule;
    private PoolScheduleDTO scheduleDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        pool = new Pool();
        pool.setId(1);
        pool.setName("Main Pool");
        pool.setMaxCapacity(20);

        poolDTO = new PoolDTO();
        poolDTO.setName("Main Pool");
        poolDTO.setMaxCapacity(20);

        schedule = new PoolSchedule();
        schedule.setId(1);
        schedule.setPool(pool);
        schedule.setDayOfWeek((short) 1);
        schedule.setOpeningTime(LocalTime.of(8, 0));
        schedule.setClosingTime(LocalTime.of(20, 0));

        scheduleDTO = new PoolScheduleDTO();
        scheduleDTO.setPoolName("Main Pool");
        scheduleDTO.setDayOfWeek((short) 1);
        scheduleDTO.setOpeningTime(LocalTime.of(8, 0));
        scheduleDTO.setClosingTime(LocalTime.of(20, 0));
    }

    @Test
    void test_createPool_shouldReturnSavedPoolDTO() {
        when(poolRepository.save(any())).thenReturn(pool);

        PoolDTO result = poolService.createPool(poolDTO);

        assertNotNull(result);
        assertEquals(pool.getName(), result.getName());
        verify(poolRepository).save(any());
    }

    @Test
    void test_getPoolByName_shouldReturnPoolIfFound() {
        when(poolRepository.findByName("Main Pool")).thenReturn(Optional.of(pool));
        Pool result = poolService.getPoolByName("Main Pool");
        assertEquals(pool, result);
    }

    @Test
    void test_getPoolByName_shouldThrowIfNotFound() {
        when(poolRepository.findByName("Main Pool")).thenReturn(Optional.empty());
        assertThrows(ModelNotFoundException.class, () -> poolService.getPoolByName("Main Pool"));
    }

    @Test
    void test_searchPools_shouldFilterByName() {
        PoolDTO filterDto = PoolDTO.builder()
                .name("main")
                .build();
        when(poolRepository.findPoolByFilter("main", null, null, null, null))
                .thenReturn(List.of(pool));

        List<PoolDTO> result = poolService.searchPools(filterDto);

        assertEquals(1, result.size());
        assertEquals("Main Pool", result.get(0).getName());
    }

    @Test
    void test_searchPools_shouldFilterByMultipleFields() {
        PoolDTO filterDto = PoolDTO.builder()
                .name("main")
                .address("Some Address")
                .maxCapacity(20)
                .sessionDurationMinutes(60)
                .build();
        when(poolRepository.findPoolByFilter("main", "Some Address", null, 20, 60))
                .thenReturn(List.of(pool));

        List<PoolDTO> result = poolService.searchPools(filterDto);

        assertEquals(1, result.size());
        assertEquals("Main Pool", result.get(0).getName());
    }

    @Test
    void test_searchPools_shouldReturnAllIfNoFilter() {
        PoolDTO filterDto = PoolDTO.builder().build();
        when(poolRepository.findPoolByFilter(null, null, null, null, null))
                .thenReturn(List.of(pool));

        List<PoolDTO> result = poolService.searchPools(filterDto);

        assertEquals(1, result.size());
        assertEquals("Main Pool", result.get(0).getName());
    }


    @Test
    void test_updatePool_shouldMergeAndSave() {
        PoolDTO updated = new PoolDTO();
        updated.setName("Updated Pool");
        updated.setMaxCapacity(30);

        when(poolRepository.findByName("Main Pool")).thenReturn(Optional.of(pool));
        when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PoolDTO result = poolService.updatePool("Main Pool", updated);

        assertEquals("Updated Pool", result.getName());
        assertEquals(30, result.getMaxCapacity());
    }


    @Test
    void test_deletePool_shouldDeleteIfExists() {
        when(poolRepository.findByName("Main Pool")).thenReturn(Optional.of(pool));
        poolService.deletePool(poolDTO);
        verify(poolRepository).deleteByName(pool.getName());
    }

    @Test
    void test_deletePool_shouldThrowIfNotExists() {
        when(poolRepository.findByName("Main Pool")).thenReturn(Optional.empty());
        assertThrows(ModelNotFoundException.class, () -> poolService.deletePool(poolDTO));
    }

    @Test
    void test_updateCapacity_shouldSetAndSaveNewCapacity() {
        when(poolRepository.findByName("Main Pool")).thenReturn(Optional.of(pool));
        poolDTO.setMaxCapacity(40);
        when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PoolDTO result = poolService.updateCapacity(poolDTO);
        assertNotNull(result);
        assertEquals(40, result.getMaxCapacity());
    }

    @Test
    void test_createOrUpdateSchedule_shouldCreateNewIfNotExists() {
        when(poolRepository.findByName("Main Pool")).thenReturn(Optional.of(pool));
        when(scheduleRepository.findByPoolNameAndDayOfWeek("Main Pool", (short) 1)).thenReturn(Optional.empty());
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PoolScheduleDTO result = poolService.createOrUpdateSchedule(scheduleDTO);
        assertEquals(scheduleDTO.getDayOfWeek(), result.getDayOfWeek());
    }

    @Test
    void test_createOrUpdateSchedule_shouldUpdateIfExists() {
        when(poolRepository.findByName("Main Pool")).thenReturn(Optional.of(pool));
        when(scheduleRepository.findByPoolNameAndDayOfWeek("Main Pool", (short) 1)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PoolScheduleDTO result = poolService.createOrUpdateSchedule(scheduleDTO);
        assertEquals(scheduleDTO.getOpeningTime(), result.getOpeningTime());
    }

    @Test
    void test_updateSchedule_shouldUpdateIfExists() {
        when(scheduleRepository.findByPoolNameAndDayOfWeek("Main Pool", (short) 1)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PoolScheduleDTO result = poolService.updateSchedule(scheduleDTO);
        assertEquals(scheduleDTO.getClosingTime(), result.getClosingTime());
    }

    @Test
    void test_updateSchedule_shouldThrowIfNotFound() {
        when(scheduleRepository.findByPoolNameAndDayOfWeek("Main Pool", (short) 1)).thenReturn(Optional.empty());
        assertThrows(ModelNotFoundException.class, () -> poolService.updateSchedule(scheduleDTO));
    }

    @Test
    void test_deleteScheduleByDay_shouldDeleteByDayOfWeek() {
        when(poolRepository.findByName("Main Pool")).thenReturn(Optional.of(pool));
        when(scheduleRepository.existsByPoolIdAndDayOfWeek(pool.getId(), (short) 1)).thenReturn(true);

        poolService.deleteScheduleByDay(poolDTO, (short) 1);
        verify(scheduleRepository).deleteByPoolIdAndDayOfWeek(pool.getId(), (short) 1);
    }

    @Test
    void test_getSchedulesForPool_shouldReturnList() {
        when(poolRepository.findByName("Main Pool")).thenReturn(Optional.of(pool));
        when(scheduleRepository.findByPoolId(pool.getId())).thenReturn(List.of(schedule));
        List<PoolScheduleDTO> result = poolService.getSchedulesForPool(poolDTO);
        assertEquals(1, result.size());
        assertEquals((short) 1, result.get(0).getDayOfWeek());
    }

    @Test
    void test_getPoolsByDayOfWeek_shouldReturnPools() {
        when(scheduleRepository.findPoolsByDayOfWeek((short) 1)).thenReturn(List.of(pool));
        List<PoolDTO> result = poolService.getPoolsByDayOfWeek((short) 1);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Main Pool", result.get(0).getName());
        verify(scheduleRepository).findPoolsByDayOfWeek((short) 1);
    }

    @Test
    void test_deleteScheduleByDay_shouldThrowIfNotExists() {
        when(poolRepository.findByName("Main Pool")).thenReturn(Optional.of(pool));
        when(scheduleRepository.existsByPoolIdAndDayOfWeek(pool.getId(), (short) 1)).thenReturn(false);

        assertThrows(ModelNotFoundException.class,
                () -> poolService.deleteScheduleByDay(poolDTO, (short) 1));

        verify(poolRepository).findByName("Main Pool");
        verify(scheduleRepository).existsByPoolIdAndDayOfWeek(pool.getId(), (short) 1);
    }

    @Test
    void test_createOrUpdateSchedule_shouldThrowIfPoolNotFound() {
        when(poolRepository.findByName("Main Pool")).thenReturn(Optional.empty());
        assertThrows(ModelNotFoundException.class,
                () -> poolService.createOrUpdateSchedule(scheduleDTO));
    }

    @Test
    void test_updatePool_shouldThrowIfNotFound() {
        when(poolRepository.findByName("Main Pool")).thenReturn(Optional.empty());
        assertThrows(ModelNotFoundException.class,
                () -> poolService.updatePool("Main Pool", poolDTO));
    }


}
