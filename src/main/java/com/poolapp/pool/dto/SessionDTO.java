package com.poolapp.pool.dto;

import com.poolapp.pool.dto.validation.HasTimeRange;
import com.poolapp.pool.validation.EndTimeAfterStartTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EndTimeAfterStartTime
public class SessionDTO implements HasTimeRange {

    @Size(max = 255)
    @NotNull
    private String poolName;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;
}
