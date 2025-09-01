package com.poolapp.pool.dto.requestDTO;

import com.poolapp.pool.dto.validation.UpdateValidation;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestPoolScheduleDTO {

    @NotNull(groups = UpdateValidation.class)
    @Size(max = 255)
    private String poolName;

    @NotNull
    @Min(1)
    @Max(7)
    private Short dayOfWeek;

    @NotBlank(groups = UpdateValidation.class)
    private LocalTime openingTime;

    private LocalTime closingTime;

    @AssertTrue
    public boolean isOpeningBeforeClosing() {
        if (openingTime == null || closingTime == null) {
            return true;
        }
        return openingTime.isBefore(closingTime);
    }
}

