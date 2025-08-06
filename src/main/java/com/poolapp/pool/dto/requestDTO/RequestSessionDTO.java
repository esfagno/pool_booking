package com.poolapp.pool.dto.requestDTO;

import com.poolapp.pool.dto.validation.UpdateValidation;
import com.poolapp.pool.validation.EndTimeAfterStartTime;
import jakarta.validation.constraints.NotBlank;
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
public class RequestSessionDTO {

    @Size(max = 255)
    @NotBlank(groups = UpdateValidation.class)
    private String poolName;

    @NotBlank(groups = UpdateValidation.class)
    private LocalDateTime startTime;

    private LocalDateTime endTime;


}

