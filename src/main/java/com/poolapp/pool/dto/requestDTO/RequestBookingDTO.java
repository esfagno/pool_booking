package com.poolapp.pool.dto.requestDTO;

import com.poolapp.pool.model.enums.BookingStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
public class RequestBookingDTO {

    @NotBlank
    @Size(max = 255)
    @Email
    private String userEmail;

    @Valid
    private RequestSessionDTO requestSessionDTO;

    private LocalDateTime bookingTime;

    private BookingStatus status;

}
