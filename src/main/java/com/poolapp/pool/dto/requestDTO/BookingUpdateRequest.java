package com.poolapp.pool.dto.requestDTO;

import com.poolapp.pool.dto.BookingDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BookingUpdateRequest {
    @Valid
    private BookingDTO currentBooking;
    @Valid
    private BookingDTO newBooking;
}
