package com.poolapp.pool.service.impl;

import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.model.Pool;
import com.poolapp.pool.model.PoolSchedule;
import com.poolapp.pool.repository.PoolRepository;
import com.poolapp.pool.repository.PoolScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PoolServiceImplTest {

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private PoolScheduleRepository scheduleRepository;

    @InjectMocks
    private PoolServiceImpl poolService;

    private Pool pool;
    private PoolSchedule schedule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        pool = new Pool();
        pool.setId(1);
        pool.setName("Main Pool");
        pool.setMaxCapacity(20);

        schedule = new PoolSchedule();
        schedule.setId(1);
        schedule.setPool(pool);
        schedule.setDayOfWeek((short) 1);
        schedule.setOpeningTime(LocalTime.of(8, 0));
        schedule.setClosingTime(LocalTime.of(20, 0));
    }

    @Test
    void test_createPool_shouldReturnSavedPool() {
        when(poolRepository.save(pool)).thenReturn(pool);
        Pool result = poolService.createPool(pool);
        assertEquals(pool, result);
        verify(poolRepository).save(pool);
    }

    @Test
    void test_getPoolById_shouldReturnPoolIfFound() {
        when(poolRepository.findById(1)).thenReturn(Optional.of(pool));
        Pool result = poolService.getPoolById(1);
        assertEquals(pool, result);
    }

    @Test
    void test_getPoolById_shouldThrowIfNotFound() {
        when(poolRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ModelNotFoundException.class, () -> poolService.getPoolById(1));
    }

    @Test
    void test_getAllPools_shouldFilterByName() {
        Pool another = new Pool();
        another.setId(2);
        another.setName("Secondary Pool");

        when(poolRepository.findByNameContainingNullable("main")).thenReturn(List.of(pool));
        List<Pool> result = poolService.getAllPools("main");

        assertEquals(1, result.size());
        assertEquals("Main Pool", result.get(0).getName());
    }

    @Test
    void test_getAllPools_shouldReturnAllIfNoFilter() {
        when(poolRepository.findByNameContainingNullable(null)).thenReturn(List.of(pool));
        List<Pool> result = poolService.getAllPools(null);
        assertEquals(1, result.size());
    }

    @Test
    void test_updatePool_shouldMergeAndSave() {
        Pool updated = new Pool();
        updated.setName("Updated Pool");

        when(poolRepository.findById(1)).thenReturn(Optional.of(pool));
        when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Pool result = poolService.updatePool(1, updated);
        assertEquals("Updated Pool", result.getName());
    }

    @Test
    void test_deletePool_shouldDeleteIfExists() {
        when(poolRepository.existsById(1)).thenReturn(true);
        poolService.deletePool(1);
        verify(poolRepository).deleteById(1);
    }

    @Test
    void test_deletePool_shouldThrowIfNotExists() {
        when(poolRepository.existsById(1)).thenReturn(false);
        assertThrows(ModelNotFoundException.class, () -> poolService.deletePool(1));
    }

    @Test
    void test_updateCapacity_shouldSetAndSaveNewCapacity() {
        when(poolRepository.findById(1)).thenReturn(Optional.of(pool));
        when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Pool result = poolService.updateCapacity(1, 30);
        assertEquals(30, result.getMaxCapacity());
    }

    @Test
    void test_createOrUpdateSchedule_shouldCreateNewIfNotExists() {
        when(poolRepository.findById(1)).thenReturn(Optional.of(pool));
        when(scheduleRepository.findByPoolIdAndDayOfWeek(1, (short) 1)).thenReturn(Optional.empty());
        when(scheduleRepository.save(schedule)).thenReturn(schedule);

        PoolSchedule result = poolService.createOrUpdateSchedule(1, schedule);
        assertEquals(schedule, result);
    }

    @Test
    void test_createOrUpdateSchedule_shouldUpdateIfExists() {
        PoolSchedule existing = new PoolSchedule();
        existing.setId(2);
        existing.setPool(pool);
        existing.setDayOfWeek((short) 1);
        existing.setOpeningTime(LocalTime.of(7, 0));
        existing.setClosingTime(LocalTime.of(21, 0));

        when(poolRepository.findById(1)).thenReturn(Optional.of(pool));
        when(scheduleRepository.findByPoolIdAndDayOfWeek(1, (short) 1)).thenReturn(Optional.of(existing));
        when(scheduleRepository.save(existing)).thenReturn(existing);

        PoolSchedule result = poolService.createOrUpdateSchedule(1, schedule);
        assertEquals(existing, result);
    }

    @Test
    void test_updateSchedule_shouldUpdateIfExists() {
        PoolSchedule updated = new PoolSchedule();
        updated.setOpeningTime(LocalTime.of(9, 0));
        updated.setClosingTime(LocalTime.of(18, 0));

        when(scheduleRepository.findById(1)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PoolSchedule result = poolService.updateSchedule(1, updated);
        assertEquals(LocalTime.of(9, 0), result.getOpeningTime());
        assertEquals(LocalTime.of(18, 0), result.getClosingTime());
    }

    @Test
    void test_updateSchedule_shouldThrowIfNotFound() {
        when(scheduleRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ModelNotFoundException.class, () -> poolService.updateSchedule(1, schedule));
    }

    @Test
    void test_deleteScheduleByDay_shouldDeleteByDayOfWeek() {
        poolService.deleteScheduleByDay(1, (short) 1);
        verify(scheduleRepository).deleteByPoolIdAndDayOfWeek(1, (short) 1);
    }

    @Test
    void test_getSchedulesForPool_shouldReturnList() {
        when(scheduleRepository.findByPoolId(1)).thenReturn(List.of(schedule));
        List<PoolSchedule> result = poolService.getSchedulesForPool(1);
        assertEquals(1, result.size());
    }

    @Test
    void test_isPoolOpenAt_shouldReturnTrueIfOpen() {
        LocalDateTime dt = LocalDateTime.of(2025, 6, 23, 10, 0);
        when(scheduleRepository.findByPoolIdAndDayOfWeek(1, (short) 1)).thenReturn(Optional.of(schedule));
        boolean result = poolService.isPoolOpenAt(1, dt);
        assertTrue(result);
    }

    @Test
    void test_isPoolOpenAt_shouldReturnFalseIfClosed() {
        LocalDateTime dt = LocalDateTime.of(2025, 6, 23, 22, 0);
        when(scheduleRepository.findByPoolIdAndDayOfWeek(1, (short) 1)).thenReturn(Optional.of(schedule));
        boolean result = poolService.isPoolOpenAt(1, dt);
        assertFalse(result);
    }

    @Test
    void test_isPoolOpenAt_shouldReturnFalseIfNoSchedule() {
        LocalDateTime dt = LocalDateTime.of(2025, 6, 23, 10, 0);
        when(scheduleRepository.findByPoolIdAndDayOfWeek(1, (short) 1)).thenReturn(Optional.empty());
        boolean result = poolService.isPoolOpenAt(1, dt);
        assertFalse(result);
    }

    @Test
    void test_getPoolsByDayOfWeek_shouldReturnPools() {
        when(scheduleRepository.findPoolsByDayOfWeek((short) 1)).thenReturn(List.of(pool));
        List<Pool> result = poolService.getPoolsByDayOfWeek((short) 1);
        assertEquals(1, result.size());
    }
}
