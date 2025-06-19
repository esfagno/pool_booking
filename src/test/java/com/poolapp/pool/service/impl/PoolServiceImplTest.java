package com.poolapp.pool.service.impl;

import com.poolapp.pool.exception.BadRequestException;
import com.poolapp.pool.exception.ResourceNotFoundException;
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
    void testCreatePool() {
        when(poolRepository.save(pool)).thenReturn(pool);
        Pool result = poolService.createPool(pool);
        assertEquals(pool, result);
        verify(poolRepository).save(pool);
    }

    @Test
    void testGetPoolByIdFound() {
        when(poolRepository.findById(1)).thenReturn(Optional.of(pool));
        Pool result = poolService.getPoolById(1);
        assertEquals(pool, result);
    }

    @Test
    void testGetPoolByIdNotFound() {
        when(poolRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> poolService.getPoolById(1));
    }

    @Test
    void testGetAllPools_FilterByName() {
        Pool another = new Pool();
        another.setId(2);
        another.setName("Secondary Pool");

        when(poolRepository.findAll()).thenReturn(List.of(pool, another));
        List<Pool> result = poolService.getAllPools("main");

        assertEquals(1, result.size());
        assertEquals("Main Pool", result.get(0).getName());
    }

    @Test
    void testGetAllPools_NoFilter() {
        when(poolRepository.findAll()).thenReturn(List.of(pool));
        List<Pool> result = poolService.getAllPools(null);
        assertEquals(1, result.size());
    }

    @Test
    void testUpdatePool() {
        Pool updated = new Pool();
        updated.setName("Updated Pool");

        when(poolRepository.findById(1)).thenReturn(Optional.of(pool));
        when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Pool result = poolService.updatePool(1, updated);
        assertEquals("Updated Pool", result.getName());
    }

    @Test
    void testDeletePool_Success() {
        when(poolRepository.existsById(1)).thenReturn(true);
        poolService.deletePool(1);
        verify(poolRepository).deleteById(1);
    }

    @Test
    void testDeletePool_NotFound() {
        when(poolRepository.existsById(1)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> poolService.deletePool(1));
    }

    @Test
    void testUpdateCapacity() {
        when(poolRepository.findById(1)).thenReturn(Optional.of(pool));
        when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Pool result = poolService.updateCapacity(1, 30);
        assertEquals(30, result.getMaxCapacity());
    }

    @Test
    void testCreateOrUpdateSchedule_CreateNew() {
        when(poolRepository.findById(1)).thenReturn(Optional.of(pool));
        when(scheduleRepository.findByPoolIdAndDayOfWeek(1, (short) 1)).thenReturn(Optional.empty());
        when(scheduleRepository.save(schedule)).thenReturn(schedule);

        PoolSchedule result = poolService.createOrUpdateSchedule(1, schedule);
        assertEquals(schedule, result);
    }

    @Test
    void testCreateOrUpdateSchedule_UpdateExisting() {
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
    void testUpdateSchedule_Success() {
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
    void testUpdateSchedule_NotFound() {
        when(scheduleRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> poolService.updateSchedule(1, schedule));
    }

    @Test
    void testDeleteScheduleByDay() {
        poolService.deleteScheduleByDay(1, (short) 1);
        verify(scheduleRepository).deleteByPoolIdAndDayOfWeek(1, (short) 1);
    }

    @Test
    void testGetSchedulesForPool() {
        when(scheduleRepository.findByPoolId(1)).thenReturn(List.of(schedule));
        List<PoolSchedule> result = poolService.getSchedulesForPool(1);
        assertEquals(1, result.size());
    }

    @Test
    void testIsPoolOpenAt_Open() {
        LocalDateTime dt = LocalDateTime.of(2025, 6, 23, 10, 0);
        when(scheduleRepository.findByPoolIdAndDayOfWeek(1, (short) 1)).thenReturn(Optional.of(schedule));
        boolean result = poolService.isPoolOpenAt(1, dt);
        assertTrue(result);
    }

    @Test
    void testIsPoolOpenAt_Closed() {
        LocalDateTime dt = LocalDateTime.of(2025, 6, 23, 22, 0);
        when(scheduleRepository.findByPoolIdAndDayOfWeek(1, (short) 1)).thenReturn(Optional.of(schedule));
        boolean result = poolService.isPoolOpenAt(1, dt);
        assertFalse(result);
    }

    @Test
    void testIsPoolOpenAt_NoSchedule() {
        LocalDateTime dt = LocalDateTime.of(2025, 6, 23, 10, 0);
        when(scheduleRepository.findByPoolIdAndDayOfWeek(1, (short) 1)).thenReturn(Optional.empty());
        boolean result = poolService.isPoolOpenAt(1, dt);
        assertFalse(result);
    }

    @Test
    void testGetPoolsByDayOfWeek_Success() {
        when(scheduleRepository.findPoolsByDayOfWeek((short) 1)).thenReturn(List.of(pool));
        List<Pool> result = poolService.getPoolsByDayOfWeek((short) 1);
        assertEquals(1, result.size());
    }

    @Test
    void testGetPoolsByDayOfWeek_Null() {
        assertThrows(BadRequestException.class, () -> poolService.getPoolsByDayOfWeek(null));
    }
}
