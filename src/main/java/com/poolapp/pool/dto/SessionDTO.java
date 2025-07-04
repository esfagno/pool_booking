package com.poolapp.pool.dto;

import com.poolapp.pool.validation.EndTimeAfterStartTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class SessionDTO {

    @Valid
    @NotBlank
    private PoolDTO poolDTO;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;
}
