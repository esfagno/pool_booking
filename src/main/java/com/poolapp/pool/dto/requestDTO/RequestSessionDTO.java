package com.poolapp.pool.dto.requestDTO;

import com.poolapp.pool.dto.RequestPoolDTO;
import com.poolapp.pool.dto.validation.HasTimeRange;
import com.poolapp.pool.dto.validation.UpdateValidation;
import com.poolapp.pool.validation.EndTimeAfterStartTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EndTimeAfterStartTime
public class RequestSessionDTO implements HasTimeRange {

    @Valid
    @NotNull(groups = UpdateValidation.class)
    private RequestPoolDTO requestPoolDTO;

    @NotBlank(groups = UpdateValidation.class)
    private LocalDateTime startTime;

    private LocalDateTime endTime;


}

