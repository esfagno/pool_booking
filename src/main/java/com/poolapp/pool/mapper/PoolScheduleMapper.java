package com.poolapp.pool.mapper;

import com.poolapp.pool.model.PoolSchedule;

public class PoolScheduleMapper {

    public static PoolSchedule updateScheduleWith(PoolSchedule existing, PoolSchedule updated) {
        existing.setOpeningTime(updated.getOpeningTime());
        existing.setClosingTime(updated.getClosingTime());
        existing.setDayOfWeek(updated.getDayOfWeek());

        return existing;
    }
}