package com.poolapp.pool.dto.validation;

import com.poolapp.pool.validation.EndTimeAfterStartTime;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EndTimeAfterStartTimeValidator
        implements ConstraintValidator<EndTimeAfterStartTime, HasTimeRange> {

    @Override
    public boolean isValid(HasTimeRange dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        var start = dto.getStartTime();
        var end = dto.getEndTime();

        if (start == null || end == null) {
            return true;
        }

        return !end.isBefore(start);
    }
}