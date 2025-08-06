package com.poolapp.pool.dto;

import com.poolapp.pool.dto.validation.UpdateValidation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestPoolDTO {

    @Size(max = 255)
    private String newName;

    @Size(max = 255)
    @NotBlank(groups = UpdateValidation.class)
    private String name;

    private String address;

    private String description;

    @Min(1)
    @NotNull(groups = UpdateValidation.class)
    private Integer maxCapacity;

    @Min(10)
    @Max(480)
    private Integer sessionDurationMinutes;
}
