package com.poolapp.pool.dto.validation;

public interface HasTimeRange {
    java.time.LocalDateTime getStartTime();
    java.time.LocalDateTime getEndTime();
}
